package model;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {
    @Test
    void testEpicTimeFields() {
        Epic epic = new Epic(1, "Epic", "Description", TaskStatus.NEW);
        
        Subtask sub1 = new Subtask(2, "Sub1", "Description1", TaskStatus.NEW, 1, 
                                   Duration.ofMinutes(30), 
                                   LocalDateTime.of(2024, 6, 1, 9, 0));
        
        Subtask sub2 = new Subtask(3, "Sub2", "Description2", TaskStatus.NEW, 1,
                                   Duration.ofMinutes(60),
                                   LocalDateTime.of(2024, 6, 1, 11, 0));
        
        List<Subtask> subtasks = List.of(sub1, sub2);

        epic.updateTimeFields(subtasks);

        assertEquals(Duration.ofMinutes(90), epic.getDuration());
        assertEquals(LocalDateTime.of(2024, 6, 1, 9, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2024, 6, 1, 12, 0), epic.getEndTime());
    }
}