package util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.*;

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
    void testCreateAndGetTask() {
        Task task = new Task("Task", "Desc");
        manager.createTask(task);
        assertEquals(task, manager.getTaskById(task.getId()).orElse(null));
    }

    @Test
    void testCreateAndGetEpicAndSubtask() {
        Epic epic = new Epic("Epic", "Desc");
        manager.createEpic(epic);
        Subtask sub = new Subtask("Sub", "Desc", epic.getId());
        manager.createSubtask(sub);
        assertEquals(epic, manager.getEpicById(epic.getId()).orElse(null));
        assertEquals(sub, manager.getSubtaskById(sub.getId()).orElse(null));
        assertTrue(manager.getSubtasksOfEpic(epic.getId()).contains(sub));
    }

    @Test
    void testEpicStatusAllNew() {
        Epic epic = new Epic("Epic", "Desc");
        manager.createEpic(epic);
        Subtask s1 = new Subtask("S1", "D1", epic.getId());
        Subtask s2 = new Subtask("S2", "D2", epic.getId());
        manager.createSubtask(s1);
        manager.createSubtask(s2);
        assertEquals(TaskStatus.NEW, manager.getEpicById(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void testEpicStatusAllDone() {
        Epic epic = new Epic("Epic", "Desc");
        manager.createEpic(epic);
        Subtask s1 = new Subtask("S1", "D1", epic.getId());
        Subtask s2 = new Subtask("S2", "D2", epic.getId());
        s1.setStatus(TaskStatus.DONE);
        s2.setStatus(TaskStatus.DONE);
        manager.createSubtask(s1);
        manager.createSubtask(s2);
        assertEquals(TaskStatus.DONE, manager.getEpicById(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void testEpicStatusMixed() {
        Epic epic = new Epic("Epic", "Desc");
        manager.createEpic(epic);
        Subtask s1 = new Subtask("S1", "D1", epic.getId());
        Subtask s2 = new Subtask("S2", "D2", epic.getId());
        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.DONE);
        manager.createSubtask(s1);
        manager.createSubtask(s2);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void testEpicStatusInProgress() {
        Epic epic = new Epic("Epic", "Desc");
        manager.createEpic(epic);
        Subtask s1 = new Subtask("S1", "D1", epic.getId());
        Subtask s2 = new Subtask("S2", "D2", epic.getId());
        s1.setStatus(TaskStatus.IN_PROGRESS);
        s2.setStatus(TaskStatus.IN_PROGRESS);
        manager.createSubtask(s1);
        manager.createSubtask(s2);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void testPrioritizedTasksOrder() {
        Task t1 = new Task("T1", "D1");
        t1.setStartTime(LocalDateTime.of(2025, 10, 2, 10, 0));
        t1.setDuration(Duration.ofMinutes(30));
        Task t2 = new Task("T2", "D2");
        t2.setStartTime(LocalDateTime.of(2025, 10, 2, 9, 0));
        t2.setDuration(Duration.ofMinutes(30));
        manager.createTask(t1);
        manager.createTask(t2);
        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(t2, prioritized.get(0));
        assertEquals(t1, prioritized.get(1));
    }

    @Test
    void testTimeIntersection() {
        Task t1 = new Task("T1", "D1");
        t1.setStartTime(LocalDateTime.of(2025, 10, 2, 10, 0));
        t1.setDuration(Duration.ofMinutes(60));
        manager.createTask(t1);
        Task t2 = new Task("T2", "D2");
        t2.setStartTime(LocalDateTime.of(2025, 10, 2, 10, 30));
        t2.setDuration(Duration.ofMinutes(30));
        assertThrows(IllegalArgumentException.class, () -> manager.createTask(t2));
    }
}