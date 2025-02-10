package task;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subTaskList = new ArrayList<>();

    public Epic(String name, String description) {

        super(name, description, Status.NEW);
    }

    public Epic(Epic epic) {
        super(epic);
        subTaskList.addAll(epic.subTaskList);
    }


    @Override
    public String toString() {
        return "Epic{" +
                "uid=" + getUid() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subTaskList=" + subTaskList +
                '}';
    }

    public List<Integer> getSubTaskList() {

        return subTaskList;
    }

    public void addSubTaskList(int uid) {
        if (!subTaskList.contains(uid)) {
            subTaskList.add(uid);
        }
    }

    public void removeSubTaskList(int uid) {
        subTaskList.remove(uid);
    }

    public void cleanSubTaskList() {
        subTaskList.clear();
    }


}
