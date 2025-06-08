package model;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @Test
    void testDurationAndStartTime() {
        Duration duration = Duration.ofMinutes(90);
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 1, 10, 0);
        Task task = new Task(1, "Task", "Description", TaskStatus.NEW, duration, startTime);

        assertEquals(duration, task.getDuration());
        assertEquals(startTime, task.getStartTime());
        assertEquals(startTime.plus(duration), task.getEndTime());
    }
}