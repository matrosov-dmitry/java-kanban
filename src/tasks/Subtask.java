package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private int epicId; // эпик, на который ссылается подзадача

    public Subtask() {
        super();
        this.epicId = 0;
    }

    public Subtask(int id, String title, String description, TaskStatus status, int epicId) {
        super(id, title, description, status);
        this.epicId = epicId;
    }

    public Subtask(String title, String description) {
        super(title, description);
        this.epicId = 0;
    }

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(id, title, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(String title, String description, int epicId, Duration duration, LocalDateTime startTime) {
        super(title, description);
        this.epicId = epicId;
        this.duration = duration;
        this.startTime = startTime;
    }

    // Добавлен вспомогательный конструктор для тестов: со статусом, длительностью в минутах и временем начала
    public Subtask(String title, String description, TaskStatus status, int durationMinutes, LocalDateTime startTime) {
        super(title, description, status, durationMinutes, startTime);
        this.epicId = 0;
    }

    // Добавлен вспомогательный конструктор для тестов: только со статусом
    public Subtask(String title, String description, TaskStatus status) {
        super(title, description, status);
        this.epicId = 0;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "\nSubtask{" +
                " id=" + id +
                ", epicId=" + epicId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }
}