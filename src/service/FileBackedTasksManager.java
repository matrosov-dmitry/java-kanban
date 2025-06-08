package service;

import model.Task;
import model.Subtask;
import model.Epic;
import model.TaskStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private final File file;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
        Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Task::getId)
    );

    public FileBackedTasksManager(File file) {
        this.file = file;
    }

    // Проверка пересечения двух задач по времени
    private boolean isTimeOverlap(Task t1, Task t2) {
        if (t1.getStartTime() == null || t1.getEndTime() == null ||
            t2.getStartTime() == null || t2.getEndTime() == null) {
            return false;
        }
        return !t1.getEndTime().isBefore(t2.getStartTime()) && !t1.getStartTime().isAfter(t2.getEndTime());
    }

    // Проверка, пересекается ли задача с любой другой в менеджере
    private boolean hasTimeOverlap(Task task) {
        return prioritizedTasks.stream()
                .filter(t -> t.getId() != task.getId())
                .anyMatch(t -> isTimeOverlap(t, task));
    }

    @Override
    public void addTask(Task task) {
        if (task.getStartTime() != null && hasTimeOverlap(task)) {
            throw new IllegalArgumentException("Время задачи пересекается с другой задачей");
        }
        super.addTask(task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask.getStartTime() != null && hasTimeOverlap(subtask)) {
            throw new IllegalArgumentException("Время подзадачи пересекается с другой задачей");
        }
        super.addSubtask(subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
    }

    @Override
    public void removeTask(int id) {
        Task task = super.getTask(id);
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
        super.removeTask(id);
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        if (subtask != null && subtask.getStartTime() != null) {
            prioritizedTasks.remove(subtask);
        }
        super.removeSubtask(id);
    }

    @Override
    public void updateTask(Task task) {
        if (task.getStartTime() != null && hasTimeOverlap(task)) {
            throw new IllegalArgumentException("Время задачи пересекается с другой задачей");
        }
        Task oldTask = super.getTask(task.getId());
        if (oldTask != null && oldTask.getStartTime() != null) {
            prioritizedTasks.remove(oldTask);
        }
        super.updateTask(task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask.getStartTime() != null && hasTimeOverlap(subtask)) {
            throw new IllegalArgumentException("Время подзадачи пересекается с другой задачей");
        }
        Subtask oldSubtask = super.getSubtask(subtask.getId());
        if (oldSubtask != null && oldSubtask.getStartTime() != null) {
            prioritizedTasks.remove(oldSubtask);
        }
        super.updateSubtask(subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Запись заголовка
            writer.write("id,name,description,status,duration,startTime,type,epic\n");

            // Запись задач
            for (Task task : tasks.values()) {
                writer.write(taskToString(task) + "\n");
            }

            // Запись эпиков
            for (Epic epic : epics.values()) {
                writer.write(taskToString(epic) + "\n");
            }

            // Запись подзадач
            for (Subtask subtask : subtasks.values()) {
                writer.write(taskToString(subtask) + "\n");
            }

            // Добавление пустой строки и истории просмотров
            writer.write("\n");
            // Здесь должен быть код для сохранения истории просмотров
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении в файл", e);
        }
    }

    private String taskToString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getName()).append(",");
        sb.append(task.getDescription()).append(",");
        sb.append(task.getStatus()).append(",");

        String durationStr = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String startTimeStr = task.getStartTime() != null ? task.getStartTime().toString() : "";

        sb.append(durationStr).append(",");
        sb.append(startTimeStr).append(",");

        if (task instanceof Epic) {
            sb.append("EPIC");
        } else if (task instanceof Subtask) {
            sb.append("SUBTASK").append(",");
            sb.append(((Subtask) task).getEpicId());
        } else {
            sb.append("TASK");
        }

        return sb.toString();
    }

    private Task taskFromString(String value) {
        String[] fields = value.split(",");

        int id = Integer.parseInt(fields[0]);
        String name = fields[1];
        String description = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);

        // Индексы для duration и startTime
        int durationIndex = 4;
        int startTimeIndex = 5;
        int typeIndex = 6;

        Duration duration = fields.length > durationIndex && !fields[durationIndex].isEmpty()
                ? Duration.ofMinutes(Long.parseLong(fields[durationIndex]))
                : null;
        LocalDateTime startTime = fields.length > startTimeIndex && !fields[startTimeIndex].isEmpty()
                ? LocalDateTime.parse(fields[startTimeIndex])
                : null;

        String type = fields[typeIndex];

        switch (type) {
            case "TASK":
                return new Task(id, name, description, status, duration, startTime);
            case "SUBTASK":
                int epicId = Integer.parseInt(fields[7]);
                return new Subtask(id, name, description, status, epicId, duration, startTime);
            case "EPIC":
                return new Epic(id, name, description, status);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }
}