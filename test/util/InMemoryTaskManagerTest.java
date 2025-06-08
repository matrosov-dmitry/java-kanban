package util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    // Проверка: создаётся обычная задача и корректно извлекается по ID
    @Test
    void shouldCreateAndRetrieveTask() {
        Task task = new Task("Task", "Description");
        Task created = taskManager.createTask(task);
        Task retrieved = taskManager.getTaskById(created.getId());
        assertEquals(created, retrieved);
    }

    // Проверка: создаётся эпик и к нему корректно добавляется подзадача
    @Test
    void shouldCreateEpicAndItsSubtasksCorrectly() {
        Epic epic = new Epic("Epic", "Epic Desc");
        epic = taskManager.createEpic(epic);
        Subtask sub = new Subtask("Sub", "Sub Desc");
        sub.setEpicId(epic.getId());
        sub = taskManager.createSubtask(sub);
        assertTrue(taskManager.getEpicById(epic.getId()).getSubtaskId().contains(sub.getId()));
    }

    // Проверка: после удаления подзадачи, её ID исчезает из эпика
    @Test
    void shouldRemoveSubtaskFromEpicOnDeletion() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "E"));
        Subtask sub = new Subtask("Sub", "S");
        sub.setEpicId(epic.getId());
        sub = taskManager.createSubtask(sub);
        taskManager.deleteSubtaskById(sub.getId());
        assertFalse(taskManager.getEpicById(epic.getId()).getSubtaskId().contains(sub.getId()));
    }

    // Проверка: изменения через сеттеры не разрушают целостность данных в менеджере
    @Test
    void modifyingTaskShouldNotCorruptManagerState() {
        Task task = taskManager.createTask(new Task("Task", "Desc"));
        task.setName("Changed");
        Task stored = taskManager.getTaskById(task.getId());
        assertEquals("Changed", stored.getName());
        stored.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, taskManager.getTaskById(task.getId()).getStatus());
    }
}