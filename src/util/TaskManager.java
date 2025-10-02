package util;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TaskManager {

    // Создание задач
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    // Получение списков задач
    ArrayList<Task> getAllTasks();

    ArrayList<Subtask> getAllSubtasks();

    ArrayList<Epic> getAllEpics();

    ArrayList<Subtask> getSubtasksOfEpic(int epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks(); // Метод для получения приоритетных задач


    // Обновление задач
    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    // Удаление задач
    void clearAll();

    void clearAllTasks();

    void clearAllEpics();

    void deleteAllSubtasks();

    void deleteTaskById(int id);

    void deleteSubtaskById(int id);

    void deleteEpicById(int id);

    Optional<Task> getTaskById(int id);

    Optional<Subtask> getSubtaskById(int id);

    Optional<Epic> getEpicById(int id);
}