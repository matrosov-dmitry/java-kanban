package tasks;

public class Subtask extends Task {

  private int epicId; // эпик, на который ссылается подзадача

  public Subtask(int id, String title, String description, TaskStatus status, int epicId) {
    super(id, title, description, status);
    this.epicId = epicId;
  }

  public Subtask(String title, String description) {
    super(title, description);
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
        '}';
  }
}
