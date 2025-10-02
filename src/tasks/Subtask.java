package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private int epicId; // эпик, на который ссылается подзадача

    public Subtask(int id, String title, String description, TaskStatus status, int epicId) {
        super(id, title, description, status);
        this.epicId = epicId;
    }

    public Subtask(String title, String description) {
        super(title, description);
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