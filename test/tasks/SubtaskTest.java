package tasks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

  // Объявление переменных для тестирования Epic и Subtask
  private static Epic epic;
  private static Subtask subtask1;
  private static Subtask subtask2;

  @BeforeAll
  static void init() {
    // Инициализация объектов Subtask перед выполнением тестов
    subtask1 = new Subtask("Subtask-1", "some text");
    subtask2 = new Subtask("Subtask-2", "some text 2");
  }

  @Test
  void mustBeEqualsById() {
    // Установка одинакового ID для двух задач
    subtask2.setId(subtask1.getId());
    // Проверка, что задачи равны по ID
    assertEquals(subtask1, subtask2, "Tasks are not equal");
  }

  @Test
  void durationAndStartTimeAndEndTimeWorkCorrectly() {
    Duration duration = Duration.ofMinutes(45);
    LocalDateTime start = LocalDateTime.of(2025, 10, 2, 14, 0);
    subtask1.setDuration(duration);
    subtask1.setStartTime(start);
    assertEquals(duration, subtask1.getDuration());
    assertEquals(start, subtask1.getStartTime());
    assertEquals(start.plus(duration), subtask1.getEndTime());
  }
}