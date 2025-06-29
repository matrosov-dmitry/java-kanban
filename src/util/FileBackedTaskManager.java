package util;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // --- Публичные методы ---

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        if (!file.exists() || file.length() == 0) {
            return manager;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            // Пропускаем строку заголовка
            reader.readLine();

            String line;
            List<Integer> historyIds = null;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    line = reader.readLine();
                    if (line != null && !line.isBlank()) {
                        historyIds = historyFromString(line);
                    }
                    break;
                }

                Task task = fromString(line);

                if (task instanceof Epic) {
                    manager.createEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    manager.createSubtask((Subtask) task);
                } else {
                    manager.createTask(task);
                }

                if (task.getId() >= manager.getNextId()) {
                    manager.setNextId(task.getId() + 1);
                }
            }

            if (historyIds != null && !historyIds.isEmpty()) {
                for (Integer id : historyIds) {
                    Task task = manager.getTaskById(id);
                    if (task == null) {
                        task = manager.getEpicById(id);
                    }
                    if (task == null) {
                        task = manager.getSubtaskById(id);
                    }
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла: " + e.getMessage(), e);
        }

        return manager;
    }

    @Override
    public int getNextId() {
        return super.getNextId();
    }

    @Override
    public void setNextId(int id) {
        super.setNextId(id);
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void clearAll() {
        super.clearAll();
        save();
    }

    @Override
    public void clearAllTasks() {
        super.clearAllTasks();
        save();
    }

    @Override
    public void clearAllEpics() {
        super.clearAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    // --- Приватные методы ---

    private static String historyToString(List<Task> history) {
        if (history == null || history.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Task task : history) {
            sb.append(task.getId()).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private static List<Integer> historyFromString(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        String[] parts = value.split(",");
        List<Integer> history = new ArrayList<>();
        for (String part : parts) {
            try {
                history.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                throw new ManagerSaveException("Некорректный формат идентификатора в истории: " + part);
            }
        }
        return history;
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");

        if (parts.length < 5) {
            throw new ManagerSaveException("Некорректный формат строки в файле: " + value);
        }

        int id;
        try {
            id = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            throw new ManagerSaveException("Некорректный ID задачи: " + parts[0]);
        }

        String type = parts[1];
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3].toUpperCase());
        String description = parts[4];

        switch (type) {
            case "TASK":
                Task task = new Task(name, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            case "EPIC":
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case "SUBTASK":
                if (parts.length < 6) {
                    throw new ManagerSaveException("Некорректный формат подзадачи: " + value);
                }
                int epicId;
                try {
                    epicId = Integer.parseInt(parts[5]);
                } catch (NumberFormatException e) {
                    throw new ManagerSaveException("Некорректный ID эпика в подзадаче: " + parts[5]);
                }
                Subtask subtask = new Subtask(name, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                throw new ManagerSaveException("Неизвестный тип задачи: " + type);
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }

            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }

            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }

            List<Task> history = getHistory();
            if (!history.isEmpty()) {
                writer.write("\n" + historyToString(history));
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + e.getMessage(), e);
        }
    }

    private String toString(Task task) {
        StringBuilder sb = new StringBuilder();

        sb.append(task.getId()).append(",");

        if (task instanceof Epic) {
            sb.append("EPIC,");
        } else if (task instanceof Subtask) {
            sb.append("SUBTASK,");
        } else {
            sb.append("TASK,");
        }

        sb.append(task.getName()).append(",")
                .append(task.getStatus()).append(",")
                .append(task.getDescription()).append(",");

        if (task instanceof Subtask) {
            sb.append(((Subtask) task).getEpicId());
        }

        return sb.toString();
    }
}