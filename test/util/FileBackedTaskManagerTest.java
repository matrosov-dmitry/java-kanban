package util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private File tempFile;
    private FileBackedTaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("task_manager", ".csv");
        taskManager = new FileBackedTaskManager(tempFile);
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        taskManager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
        assertTrue(loadedManager.getHistory().isEmpty());
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() {
        Task task1 = new Task("Task1", "Description1");
        task1.setStatus(TaskStatus.NEW);
        Task task2 = new Task("Task2", "Description2");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        Epic epic = new Epic("Epic1", "EpicDescription");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask1", "SubtaskDescription", epic.getId());
        subtask.setStatus(TaskStatus.DONE);
        taskManager.createSubtask(subtask);

        taskManager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> tasks = loadedManager.getAllTasks();
        assertEquals(2, tasks.size());
        assertEquals(task1, tasks.get(0));
        assertEquals(task2, tasks.get(1));

        List<Epic> epics = loadedManager.getAllEpics();
        assertEquals(1, epics.size());
        assertEquals(epic, epics.get(0));

        List<Subtask> subtasks = loadedManager.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals(subtask, subtasks.get(0));
    }

    @Test
    void shouldSaveAndLoadHistory() {
        // Создаем задачи
        Task task = new Task("Task", "Description");
        task.setStatus(TaskStatus.NEW);
        task = taskManager.createTask(task);

        Epic epic = new Epic("Epic", "EpicDescription");
        epic = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "SubtaskDescription", epic.getId());
        subtask.setStatus(TaskStatus.DONE);
        subtask = taskManager.createSubtask(subtask);

        // Добавляем задачи в историю просмотров в определенном порядке
        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtask.getId());

        // Сохраняем состояние в файл
        taskManager.save();

        // Загружаем состояние из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем, что история просмотров загружена правильно
        List<Task> history = loadedManager.getHistory();

        // Проверка размера истории
        assertEquals(3, history.size(), "История должна содержать 3 задачи");

        // Проверка идентификаторов и порядка задач в истории
        assertEquals(task.getId(), history.get(0).getId());
        assertEquals(epic.getId(), history.get(1).getId());
        assertEquals(subtask.getId(), history.get(2).getId());
    }
}