package task;

public class Subtask extends Task {
    private final int epicUid;

    public Subtask(String name, String description, Status status, int epicUid) {

        super(name, description, status);
        this.epicUid = epicUid;
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        this.epicUid = subtask.getEpicUid();
    }

    public int getEpicUid() {
        return epicUid;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "indexEpic=" + epicUid +
                ", id=" + getUid() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                '}';
    }


}
