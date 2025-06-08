package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Epic extends Task {
    private LocalDateTime endTime;

    public Epic(int id, String name, String description, TaskStatus status) {
        super(id, name, description, status, Duration.ZERO, null);
    }

    public void updateTimeFields(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            this.duration = Duration.ZERO;
            this.startTime = null;
            this.endTime = null;
            return;
        }
        
        // Рассчитываем duration как сумму продолжительностей подзадач
        this.duration = subtasks.stream()
                .filter(s -> s.getDuration() != null)
                .map(Subtask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);

        // Находим самую раннюю дату начала
        this.startTime = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(t -> t != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // Находим самую позднюю дату завершения
        this.endTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(t -> t != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}