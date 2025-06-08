package util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.*;
import service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    void shouldCreateAndGetTask() {
        Task task = new Task(1, "Task1", "Desc1", TaskStatus.NEW, null, null);
        manager.addTask(task);
        Task fetched = manager.getTask(task.getId());
        assertEquals(task.getName(), fetched.getName());
        assertEquals(task.getDescription(), fetched.getDescription());
    }

    @Test
    void shouldCreateAndGetEpic() {
        Epic epic = new Epic(1, "Epic1", "EpicDesc", TaskStatus.NEW);
        manager.addTask(epic);
        Task fetched = manager.getTask(epic.getId());
        assertEquals(epic.getName(), fetched.getName());
        assertEquals(epic.getDescription(), fetched.getDescription());
    }

    @Test
    void shouldCreateAndGetSubtaskWithEpic() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW);
        manager.addTask(epic);
        Subtask subtask = new Subtask(2, "Sub", "SubDesc", TaskStatus.NEW, epic.getId(), null, null);
        manager.addSubtask(subtask);
        Subtask fetched = manager.getSubtask(subtask.getId());
        assertEquals(subtask.getName(), fetched.getName());
        assertEquals(subtask.getEpicId(), fetched.getEpicId());
        assertNotNull(manager.getTask(epic.getId()));
    }

    @Test
    void shouldReturnSubtasksOfEpic() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW);
        manager.addTask(epic);
        Subtask sub1 = new Subtask(2, "Sub1", "Desc1", TaskStatus.NEW, epic.getId(), null, null);
        Subtask sub2 = new Subtask(3, "Sub2", "Desc2", TaskStatus.NEW, epic.getId(), null, null);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);
        
        List<Subtask> subtasks = manager.getSubtasksOfEpic(epic.getId());
        
        assertTrue(subtasks.contains(sub1));
        assertTrue(subtasks.contains(sub2));
        assertEquals(2, subtasks.size());
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task(1, "Task", "Desc", TaskStatus.NEW, null, null);
        manager.addTask(task);
        task.setName("Updated");
        manager.updateTask(task);
        assertEquals("Updated", manager.getTask(task.getId()).getName());
    }

    @Test
    void shouldUpdateEpic() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW);
        manager.addTask(epic);
        epic.setName("UpdatedEpic");
        manager.updateTask(epic);
        assertEquals("UpdatedEpic", manager.getTask(epic.getId()).getName());
    }

    @Test
    void shouldUpdateSubtask() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW);
        manager.addTask(epic);
        Subtask subtask = new Subtask(2, "Sub", "Desc", TaskStatus.NEW, epic.getId(), null, null);
        manager.addSubtask(subtask);
        subtask.setName("UpdatedSub");
        manager.updateSubtask(subtask);
        assertEquals("UpdatedSub", manager.getSubtask(subtask.getId()).getName());
    }

    @Test
    void shouldDeleteTaskById() {
        Task task = new Task(1, "Task", "Desc", TaskStatus.NEW, null, null);
        manager.addTask(task);
        manager.removeTask(task.getId());
        assertNull(manager.getTask(task.getId()));
    }

    @Test
    void shouldDeleteEpicByIdAndItsSubtasks() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW);
        manager.addTask(epic);
        Subtask subtask = new Subtask(2, "Sub", "Desc", TaskStatus.NEW, epic.getId(), null, null);
        manager.addSubtask(subtask);
        
        manager.removeTask(epic.getId());
        
        assertNull(manager.getTask(epic.getId()));
        assertNull(manager.getSubtask(subtask.getId()));
    }

    @Test
    void shouldDeleteSubtaskById() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW);
        manager.addTask(epic);
        Subtask subtask = new Subtask(2, "Sub", "Desc", TaskStatus.NEW, epic.getId(), null, null);
        manager.addSubtask(subtask);
        manager.removeSubtask(subtask.getId());
        assertNull(manager.getSubtask(subtask.getId()));
    }

    // --- Тесты расчёта статуса Epic ---

    @Test
    void epicStatusShouldBeNewIfAllSubtasksNew() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW);
        manager.addTask(epic);
        manager.addSubtask(new Subtask(2, "Sub1", "Desc1", TaskStatus.NEW, epic.getId(), null, null));
        manager.addSubtask(new Subtask(3, "Sub2", "Desc2", TaskStatus.NEW, epic.getId(), null, null));
        assertEquals(TaskStatus.NEW, manager.getTask(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeDoneIfAllSubtasksDone() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW);
        manager.addTask(epic);
        Subtask sub1 = new Subtask(2, "Sub1", "Desc1", TaskStatus.NEW, epic.getId(), null, null);
        Subtask sub2 = new Subtask(3, "Sub2", "Desc2", TaskStatus.NEW, epic.getId(), null, null);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);
        
        sub1.setStatus(TaskStatus.DONE);
        sub2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);
        assertEquals(TaskStatus.DONE, manager.getTask(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressIfSubtasksNewAndDone() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW);
        manager.addTask(epic);
        Subtask sub1 = new Subtask(2, "Sub1", "Desc1", TaskStatus.NEW, epic.getId(), null, null);
        Subtask sub2 = new Subtask(3, "Sub2", "Desc2", TaskStatus.NEW, epic.getId(), null, null);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);
        
        sub2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(sub2);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getTask(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressIfAnySubtaskInProgress() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW);
        manager.addTask(epic);
        Subtask sub1 = new Subtask(2, "Sub1", "Desc1", TaskStatus.NEW, epic.getId(), null, null);
        Subtask sub2 = new Subtask(3, "Sub2", "Desc2", TaskStatus.NEW, epic.getId(), null, null);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);
        
        sub1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(sub1);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getTask(epic.getId()).getStatus());
    }

    // --- Проверка пересечения интервалов ---

    @Test
    void shouldNotAllowOverlappingTasks() {
        Task task1 = new Task(1, "Task1", "Desc1", TaskStatus.NEW, 
                              Duration.ofMinutes(60),
                              LocalDateTime.of(2024, 6, 1, 10, 0));
        manager.addTask(task1);

        Task task2 = new Task(2, "Task2", "Desc2", TaskStatus.NEW,
                              Duration.ofMinutes(60),
                              LocalDateTime.of(2024, 6, 1, 10, 30));
        assertThrows(IllegalArgumentException.class, () -> manager.addTask(task2));
    }

    @Test
    void shouldAllowNonOverlappingTasks() {
        Task task1 = new Task(1, "Task1", "Desc1", TaskStatus.NEW,
                              Duration.ofMinutes(60),
                              LocalDateTime.of(2024, 6, 1, 10, 0));
        manager.addTask(task1);

        Task task2 = new Task(2, "Task2", "Desc2", TaskStatus.NEW,
                              Duration.ofMinutes(30),
                              LocalDateTime.of(2024, 6, 1, 11, 0));
        assertDoesNotThrow(() -> manager.addTask(task2));
    }
}