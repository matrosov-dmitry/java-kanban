package util;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected int newId = 1; // Изменили с private на protected для доступа в подклассах
    private final TreeMap<LocalDateTime, List<Task>> prioritizedTasks = new TreeMap<>();

    public int getNextId() {
        return newId;
    }

    public void setNextId(int id) {
        this.newId = id;
    }

    // Создание задач
    @Override
    public Task createTask(Task task) {
        if (task.getStartTime() != null && hasIntersectionFast(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }
        task.setId(generateID());
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        return task;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask.getStartTime() != null && hasIntersectionFast(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
        }
        subtask.setId(generateID());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.getSubtaskId().add(subtask.getId());
        updateEpicStatus(epic);
        addToPrioritized(subtask);
        return subtask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(generateID());
        epics.put(epic.getId(), epic);
        return epic;
    }

    // Получение списков задач
    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        return epic.getSubtaskId().stream()
                .map(subtasks::get)
                .filter(sub -> sub != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Обновление задач
    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return;
        }
        if (task.getStartTime() != null && hasIntersectionFast(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }
        Task current = tasks.get(task.getId());
        removeFromPrioritized(current);
        current.setName(task.getName());
        current.setDescription(task.getDescription());
        current.setStatus(task.getStatus());
        current.setDuration(task.getDuration());
        current.setStartTime(task.getStartTime());
        tasks.put(task.getId(), current);
        addToPrioritized(current);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            return;
        }
        if (subtask.getStartTime() != null && hasIntersectionFast(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
        }
        Subtask current = subtasks.get(subtask.getId());
        removeFromPrioritized(current);
        int oldEpicId = current.getEpicId();
        int newEpicId = subtask.getEpicId();
        if (oldEpicId != newEpicId) {
            Epic oldEpic = epics.get(oldEpicId);
            if (oldEpic != null) {
                oldEpic.getSubtaskId().remove((Integer) subtask.getId());
                updateEpicStatus(oldEpic);
            }
            Epic newEpic = epics.get(newEpicId);
            if (newEpic != null) {
                newEpic.getSubtaskId().add(subtask.getId());
                updateEpicStatus(newEpic);
            }
        }
        current.setName(subtask.getName());
        current.setDescription(subtask.getDescription());
        current.setStatus(subtask.getStatus());
        current.setEpicId(newEpicId);
        current.setDuration(subtask.getDuration());
        current.setStartTime(subtask.getStartTime());
        subtasks.put(subtask.getId(), current);
        addToPrioritized(current);
        Epic epic = epics.get(newEpicId);
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            return;
        }
        Epic current = epics.get(epic.getId());
        current.setName(epic.getName());
        current.setDescription(epic.getDescription());
        epics.put(epic.getId(), current);
        updateEpicStatus(current);
    }

    // Удаление задач
    @Override
    public void clearAll() {
        tasks.clear();
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void clearAllTasks() {
        tasks.clear();
    }

    @Override
    public void clearAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskId().clear();
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeFromPrioritized(task);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            removeFromPrioritized(subtask);
        }
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskId().remove(Integer.valueOf(id));
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            ArrayList<Integer> subtaskIds = epic.getSubtaskId();
            for (Integer subtaskId : subtaskIds) {
                subtasks.remove(subtaskId);
            }
        }
    }

    @Override
    public Optional<Task> getTaskById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return Optional.ofNullable(task);
    }

    @Override
    public Optional<Subtask> getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return Optional.ofNullable(subtask);
    }

    @Override
    public Optional<Epic> getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return Optional.ofNullable(epic);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.values().stream().flatMap(List::stream)
                .sorted(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Task::getId))
                .collect(Collectors.toList());
    }

    // Проверка пересечения двух задач по времени
    public static boolean isTimeIntersect(Task t1, Task t2) {
        if (t1 == null || t2 == null) return false;
        if (t1.getStartTime() == null || t1.getEndTime() == null) return false;
        if (t2.getStartTime() == null || t2.getEndTime() == null) return false;
        return t1.getStartTime().isBefore(t2.getEndTime()) && t2.getStartTime().isBefore(t1.getEndTime());
    }

    // Проверка, пересекается ли задача с любой другой в менеджере
    private boolean hasIntersection(Task task) {
        return prioritizedTasks.values().stream()
                .flatMap(List::stream)
                .filter(t -> t.getId() != task.getId())
                .anyMatch(t -> isTimeIntersect(t, task));
    }

    // Быстрая проверка пересечений
    private boolean hasIntersectionFast(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) return false;
        Map.Entry<LocalDateTime, List<Task>> floor = prioritizedTasks.floorEntry(task.getStartTime());
        Map.Entry<LocalDateTime, List<Task>> ceil = prioritizedTasks.ceilingEntry(task.getStartTime());
        List<Task> neighbors = new ArrayList<>();
        if (floor != null) neighbors.addAll(floor.getValue());
        if (ceil != null && (floor == null || !ceil.getKey().equals(floor.getKey()))) neighbors.addAll(ceil.getValue());
        return neighbors.stream().anyMatch(t -> t.getId() != task.getId() && isTimeIntersect(t, task));
    }

    // Приватные методы
    private void updateEpicStatus(Epic epic) {
        boolean allDone = true;
        boolean allNew = true;

        ArrayList<Subtask> subtaskList = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskId()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                subtaskList.add(subtask);
                TaskStatus status = subtask.getStatus();
                if (status != TaskStatus.DONE) {
                    allDone = false;
                }
                if (status != TaskStatus.NEW) {
                    allNew = false;
                }
            }
        }

        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
        // Пересчёт duration, startTime, endTime
        epic.updateEpicFields(subtaskList);
    }

    private void addToPrioritized(Task task) {
        if (task.getStartTime() == null) return;
        prioritizedTasks.computeIfAbsent(task.getStartTime(), k -> new ArrayList<>()).add(task);
    }

    private void removeFromPrioritized(Task task) {
        if (task.getStartTime() == null) return;
        List<Task> list = prioritizedTasks.get(task.getStartTime());
        if (list != null) {
            list.removeIf(t -> t.getId() == task.getId());
            if (list.isEmpty()) {
                prioritizedTasks.remove(task.getStartTime());
            }
        }
    }

    private int generateID() {
        return newId++;
    }
}