package tasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

  private static Epic epic1;
  private static Epic epic2;

  @BeforeAll
  static void init() {
    epic1 = new Epic("Epic-1", "some text");
    epic2 = new Epic("Epic-2", "some text 2");
  }

  @Test
  void mustBeEqualsById() {
    epic2.setId(epic1.getId());
    assertEquals(epic1, epic2, "Epics are not equal");
  }

  @Test
  void canNotAddSelfAsSubtask() {
    Epic epic = new Epic("Epic", "Desc");
    epic.setId(1); // установила эпику Id 1
    epic.addSubtask(1); // пытаюсь добавить ID эпика в список подзадач
    assertEquals(0,
        epic.getSubtaskId().size()); // не должно добавиться, список подзадач должен быть 0
  }

  @Test
  void durationAndStartTimeAndEndTimeAreCalculatedFromSubtasks() {
    Epic epic = new Epic("Epic", "Desc");
    epic.setId(100);
    Subtask sub1 = new Subtask(1, "S1", "D1", TaskStatus.NEW, 100, Duration.ofMinutes(30), LocalDateTime.of(2025, 10, 2, 10, 0));
    Subtask sub2 = new Subtask(2, "S2", "D2", TaskStatus.NEW, 100, Duration.ofMinutes(90), LocalDateTime.of(2025, 10, 2, 12, 0));
    epic.addSubtask(sub1.getId());
    epic.addSubtask(sub2.getId());
    epic.updateEpicFields(Arrays.asList(sub1, sub2));
    assertEquals(Duration.ofMinutes(120), epic.getDuration());
    assertEquals(LocalDateTime.of(2025, 10, 2, 10, 0), epic.getStartTime());
    assertEquals(LocalDateTime.of(2025, 10, 2, 13, 30), epic.getEndTime());
  }
}