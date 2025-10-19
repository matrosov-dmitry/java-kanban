package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private ArrayList<Integer> subtaskId;
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Epic(int id, String title, String description) {
        super(id, title, description);
        this.subtaskId = new ArrayList<>();
        this.duration = Duration.ZERO;
        this.startTime = null;
        this.endTime = null;
    }

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskId = new ArrayList<>();
        this.duration = Duration.ZERO;
        this.startTime = null;
        this.endTime = null;
    }

    public ArrayList<Integer> getSubtaskId() {
        return subtaskId;
    }

    public void setSubtaskId(ArrayList<Integer> subtaskId) {
        this.subtaskId = subtaskId;
    }

    public void addSubtask(int subId) {
        if (subId == this.getId() || subtaskId.contains(subId)) {
            return;
        }
        subtaskId.add(subId);
    }

    public void updateEpicFields(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            this.duration = Duration.ZERO;
            this.startTime = null;
            this.endTime = null;
            return;
        }
        Duration totalDuration = Duration.ZERO;
        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;
        for (Subtask sub : subtasks) {
            if (sub.getDuration() != null) {
                totalDuration = totalDuration.plus(sub.getDuration());
            }
            if (sub.getStartTime() != null) {
                if (minStart == null || sub.getStartTime().isBefore(minStart)) {
                    minStart = sub.getStartTime();
                }
                LocalDateTime subEnd = sub.getEndTime();
                if (subEnd != null && (maxEnd == null || subEnd.isAfter(maxEnd))) {
                    maxEnd = subEnd;
                }
            }
        }
        this.duration = totalDuration;
        this.startTime = minStart;
        this.endTime = maxEnd;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "\nEpic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskId=" + subtaskId +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}