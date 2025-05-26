package util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

  private HistoryManager historyManager;

  @BeforeEach
  void setUp() {
    historyManager = new InMemoryHistoryManager();
  }

  // Проверка: задача добавляется в историю и возвращается корректно
  @Test
  void shouldAddTaskToHistoryAndRetrieveIt() {
    Task task = new Task(1, "Test Task", "Description", TaskStatus.NEW);
    historyManager.add(task);
    List<Task> history = historyManager.getHistory();
    assertEquals(1, history.size());
    assertEquals(task, history.get(0));
  }

  // Проверка: задача удаляется из истории по ID
  @Test
  void shouldRemoveTaskFromHistory() {
    Task task1 = new Task(1, "Task1", "Desc", TaskStatus.NEW);
    Task task2 = new Task(2, "Task2", "Desc", TaskStatus.NEW);
    historyManager.add(task1);
    historyManager.add(task2);
    historyManager.remove(1);
    List<Task> history = historyManager.getHistory();
    assertEquals(1, history.size());
    assertEquals(2, history.get(0).getId());
  }

  // Проверка: повторное добавление одной и той же задачи не дублирует её в истории
  @Test
  void shouldNotAddSameTaskTwice() {
    Task task = new Task(1, "Task", "Desc", TaskStatus.NEW);
    historyManager.add(task);
    historyManager.add(task);
    List<Task> history = historyManager.getHistory();
    assertEquals(1, history.size());
  }
}
