package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

public class InMemoryTaskManager implements TaskManager {

  private final HashMap<Integer, Task> tasks = new HashMap<>();
  private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
  private final HashMap<Integer, Epic> epics = new HashMap<>();
  private final HistoryManager historyManager = Managers.getDefaultHistory();
  private int newId = 1;


  // Создание задач
  @Override
  public Task createTask(Task task) {
    task.setId(generateID());
    tasks.put(task.getId(), task);
    return task;
  }

  @Override
  public Subtask createSubtask(Subtask subtask) {
    subtask.setId(generateID());
    subtasks.put(subtask.getId(), subtask);
    Epic epic = epics.get(subtask.getEpicId());
    epic.getSubtaskId().add(subtask.getId());
    updateEpicStatus(epic);
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
    ArrayList<Subtask> subtaskList = new ArrayList<>();
    for (int subtaskId : epic.getSubtaskId()) {
      Subtask subtask = subtasks.get(subtaskId);
      if (subtask != null) {
        subtaskList.add(subtask);
      }
    }
    return subtaskList;
  }

  // Обновление задач
  @Override
  public void updateTask(Task task) {
    if (task == null || !tasks.containsKey(task.getId())) {
      return;
    }
    Task current = tasks.get(task.getId());
    current.setName(task.getName());
    current.setDescription(task.getDescription());
    current.setStatus(task.getStatus());
    tasks.put(task.getId(), current);
  }

  @Override
  public void updateSubtask(Subtask subtask) {
    if (subtask == null || !subtasks.containsKey(subtask.getId())) {
      return;
    }
    Subtask current = subtasks.get(subtask.getId());
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
    subtasks.put(subtask.getId(), current);

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
    tasks.remove(id);
  }

  @Override
  public void deleteSubtaskById(int id) {
    Subtask subtask = subtasks.remove(id);
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
  public Task getTaskById(int id) {
    Task task = tasks.get(id);
    historyManager.add(task);
    return task;
  }

  @Override
  public Subtask getSubtaskById(int id) {
    Subtask subtask = subtasks.get(id);
    historyManager.add(subtask);
    return subtask;
  }

  @Override
  public Epic getEpicById(int id) {
    Epic epic = epics.get(id);
    historyManager.add(epic);
    return epic;
  }

  @Override
  public List<Task> getHistory() {
    return historyManager.getHistory();
  }

  // Приватные методы
  private void updateEpicStatus(Epic epic) {
    boolean allDone = true;
    boolean allNew = true;

    for (Integer subtaskId : epic.getSubtaskId()) {
      Subtask subtask = subtasks.get(subtaskId);
      if (subtask != null) {
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
  }

  private int generateID() {
    return newId++;
  }
}