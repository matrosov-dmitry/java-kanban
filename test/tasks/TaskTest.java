package tasks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

  private static Task task1;
  private static Task task2;

  @BeforeAll
  static void init() {
    task1 = new Task("Task-1", "some text");
    task2 = new Task("Task-2", "some text 2");
  }

  @Test
  void mustBeEqualsById() {
    task2.setId(task1.getId());
    assertEquals(task1, task2, "Tasks are not equal");
  }

  @Test
  void durationAndStartTimeAndEndTimeWorkCorrectly() {
    Duration duration = Duration.ofMinutes(90);
    LocalDateTime start = LocalDateTime.of(2025, 10, 2, 12, 0);
    task1.setDuration(duration);
    task1.setStartTime(start);
    assertEquals(duration, task1.getDuration());
    assertEquals(start, task1.getStartTime());
    assertEquals(start.plus(duration), task1.getEndTime());
  }
}