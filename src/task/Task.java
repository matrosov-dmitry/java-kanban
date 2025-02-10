package task;

import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private int uid;
    private Status status;

    public Task(String name, String description, Status status) {

        this.name = name;
        this.description = description;
        this.status = status;

    }

    public Task(Task task) {
        this.name = task.name;
        this.description = task.description;
        this.uid = task.uid;
        this.status = task.status;
    }


    @Override
    public boolean equals(Object o) {

        if (o == null || getClass() != o.getClass())
            return false;
        Task task = (Task) o;
        return Objects.equals(uid, task.uid);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(uid);
    }

    @Override
    public String toString() {

        return "Task{" +
                "uid= " + uid +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
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

    public int getUid() {

        return uid;
    }

    public void setUid(int uid) {

        this.uid = uid;
    }

    public Status getStatus() {

        return status;
    }

    public void setStatus(Status status) {

        this.status = status;
    }

}
