package service;

import model.Task;
import model.Epic;
import model.Subtask;
import model.TaskStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected HashMap<Integer, Task> tasks = new HashMap<>();
    protected HashMap<Integer, Epic> epics = new HashMap<>();
    protected HashMap<Integer, Subtask> subtasks = new HashMap<>();

    @Override
    public void addTask(Task task) {
        if (hasTimeOverlap(task)) {
            throw new IllegalArgumentException("Время задачи пересекается с другой задачей");
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (hasTimeOverlap(subtask)) {
            throw new IllegalArgumentException("Время подзадачи пересекается с другой задачей");
        }

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(epic.getId());
            updateEpicTimes(epic.getId());
        }
    }

    @Override
    public void removeTask(int id) {
        tasks.remove(id);
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            subtasks.remove(id);
            updateEpicStatus(epicId);
            updateEpicTimes(epicId);
        }
    }

    @Override
    public void updateTask(Task task) {
        if (hasTimeOverlap(task)) {
            throw new IllegalArgumentException("Время задачи пересекается с другой задачей");
        }

        if (task instanceof Epic) {
            epics.put(task.getId(), (Epic) task);
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
            updateEpicTimes(subtask.getEpicId());
        } else {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (hasTimeOverlap(subtask)) {
            throw new IllegalArgumentException("Время подзадачи пересекается с другой задачей");
        }

        Subtask oldSubtask = subtasks.get(subtask.getId());
        if (oldSubtask != null) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
            updateEpicTimes(subtask.getEpicId());
        }
    }

    @Override
    public Task getTask(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        } else if (epics.containsKey(id)) {
            return epics.get(id);
        }
        return null;
    }

    @Override
    public Epic getEpic(int id) {
        return epics.get(id);
    }

    @Override
    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        allTasks.addAll(subtasks.values());
        return allTasks;
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        return subtasks.values().stream()
                .filter(subtask -> subtask.getEpicId() == epicId)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> epicSubtasks = getSubtasksOfEpic(epicId);

        if (epicSubtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean hasNew = false;
        boolean hasDone = false;
        boolean hasInProgress = false;

        for (Subtask subtask : epicSubtasks) {
            switch (subtask.getStatus()) {
                case NEW:
                    hasNew = true;
                    break;
                case DONE:
                    hasDone = true;
                    break;
                case IN_PROGRESS:
                    hasInProgress = true;
                    break;
            }
        }

        if (hasInProgress || (hasNew && hasDone)) {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        } else if (hasNew && !hasDone) {
            epic.setStatus(TaskStatus.NEW);
        } else if (!hasNew && hasDone) {
            epic.setStatus(TaskStatus.DONE);
        }
    }

    private void updateEpicTimes(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> epicSubtasks = getSubtasksOfEpic(epicId);
        epic.updateTimeFields(epicSubtasks);
    }

    // Проверка пересечения двух задач по времени
    private boolean isTimeOverlap(Task t1, Task t2) {
        if (t1.getId() == t2.getId()) return false;

        if (t1.getStartTime() == null || t1.getDuration() == null ||
            t2.getStartTime() == null || t2.getDuration() == null) {
            return false;
        }

        LocalDateTime end1 = t1.getEndTime();
        LocalDateTime end2 = t2.getEndTime();

        return !end1.isBefore(t2.getStartTime()) && !t1.getStartTime().isAfter(end2);
    }

    // Проверка пересечения задачи с любой другой в менеджере
    private boolean hasTimeOverlap(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return false;
        }

        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        allTasks.addAll(subtasks.values());

        return allTasks.stream()
                .anyMatch(t -> isTimeOverlap(task, t));
    }
}