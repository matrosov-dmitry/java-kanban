package util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void emptyHistoryShouldReturnEmptyList() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void addingTaskTwiceShouldNotDuplicate() {
        Task task = new Task("T", "D");
        historyManager.add(task);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    void removeFromBeginningMiddleEnd() {
        Task t1 = new Task("T1", "D1");
        t1.setId(1);
        Task t2 = new Task("T2", "D2");
        t2.setId(2);
        Task t3 = new Task("T3", "D3");
        t3.setId(3);
        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);
        // Удаление из начала
        historyManager.remove(1);
        assertEquals(List.of(t3, t2), historyManager.getHistory());
        // Удаление из конца
        historyManager.remove(3);
        assertEquals(List.of(t2), historyManager.getHistory());
        // Удаление из середины (после повторного добавления)
        historyManager.add(t1);
        historyManager.add(t3);
        historyManager.remove(2);
        assertEquals(List.of(t3, t1), historyManager.getHistory());
    }
}