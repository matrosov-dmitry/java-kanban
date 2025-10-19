package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {

    protected String name;
    protected String description;
    protected int id;
    protected TaskStatus status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task() {
        this.id = 0;
        this.name = "";
        this.description = "";
        this.status = TaskStatus.NEW;
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    public Task(String name, String description) {
        this.id = 0;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    // Добавлен вспомогательный конструктор для тестов: со статусом, длительностью в минутах и временем начала
    public Task(String name, String description, TaskStatus status, int durationMinutes, LocalDateTime startTime) {
        this.id = 0;
        this.name = name;
        this.description = description;
        this.status = status != null ? status : TaskStatus.NEW;
        this.duration = durationMinutes > 0 ? Duration.ofMinutes(durationMinutes) : Duration.ZERO;
        this.startTime = startTime;
    }

    // Добавлен вспомогательный конструктор для тестов: со статусом, но без времени
    public Task(String name, String description, TaskStatus status) {
        this.id = 0;
        this.name = name;
        this.description = description;
        this.status = status != null ? status : TaskStatus.NEW;
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    public Task(int id, String name, String description, TaskStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    public Task(int id, String title, String description) {
        this.id = id;
        this.name = title;
        this.description = description;
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    public Task(int id, String name, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    @Override
    public String toString() {
        return "\nTask{" +
                "id= " + id +
                "| name= '" + name + '\'' +
                "| description= '" + description + '\'' +
                "| status= " + status +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(id);
    }
}