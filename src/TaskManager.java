import task.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private final HashMap<Integer, Task> tasksList = new HashMap<>();
    private final HashMap<Integer, Epic> epicsList = new HashMap<>();
    private final HashMap<Integer, Subtask> subTasksList = new HashMap<>();

    private int createUid = 1;


    // Создание

    public Task createTask(Task task) {

        task.setUid(createUid++);
        tasksList.put(task.getUid(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {

        epic.setUid(createUid++);
        epicsList.put(epic.getUid(), epic);
        return epic;
    }

    public Subtask addSubTask(Subtask subtask) {

        if (!epicsList.containsKey(subtask.getEpicUid())) {
            return subtask;
        }

        subtask.setUid(createUid++);
        subTasksList.put(subtask.getUid(), subtask);

        Epic epic = epicsList.get(subtask.getEpicUid());
        epic.addSubTaskList(subtask.getUid());
        updateEpicStatus(epic);

        return subtask;

    }

    // Обновление

    public Task updateTask(Task updatedTask) {


        if (!tasksList.containsKey(updatedTask.getUid())) {
            return updatedTask;
        }

        Task oldTask = tasksList.get(updatedTask.getUid());

        oldTask.setName(updatedTask.getName());
        oldTask.setDescription(updatedTask.getDescription());
        oldTask.setStatus(updatedTask.getStatus());


        return oldTask;
    }

    public Epic updateEpic(Epic updatedEpic) {

        if (!epicsList.containsKey(updatedEpic.getUid())) {
            return updatedEpic;
        }

        Epic oldEpic = epicsList.get(updatedEpic.getUid());
        oldEpic.setName(updatedEpic.getName());
        oldEpic.setDescription(updatedEpic.getDescription());

        return oldEpic;
    }

    public Subtask updateSubtask(Subtask updatedSubtask) {

        if (!subTasksList.containsKey(updatedSubtask.getUid())) {
            return updatedSubtask;
        }

        if (!epicsList.containsKey(updatedSubtask.getEpicUid())) {
            return updatedSubtask;
        }

        Subtask oldSubtask = subTasksList.get(updatedSubtask.getUid());

        oldSubtask.setName(updatedSubtask.getName());
        oldSubtask.setDescription(updatedSubtask.getDescription());
        oldSubtask.setStatus(updatedSubtask.getStatus());
        updateEpicStatus(epicsList.get(updatedSubtask.getEpicUid()));

        return oldSubtask;
    }


    // Вывод всех задач

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasksList.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<Epic>(epicsList.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<Subtask>(subTasksList.values());
    }

    // Вывод подзадач по номеру эпика

    public ArrayList<Subtask> getSubtaskByIndexEpic(int indexEpic) {
        Epic epic = epicsList.get(indexEpic);
        ArrayList<Subtask> subtaskArrayList = new ArrayList<>();

        if (epic != null) {

            for (Integer id : epic.getSubTaskList()) {
                Subtask subtask = subTasksList.get(id);

                if (subtask != null) {
                    subtaskArrayList.add(subtask);
                }
            }
        }
        return subtaskArrayList;
    }

    // Вызов по UID

    public Task searchTaskByUid(int uid) {

        return tasksList.get(uid);
    }

    public Epic searchEpicByUid(int uid) {

        return epicsList.get(uid);
    }

    public Subtask searchSubTaskByUid(int uid) {

        return subTasksList.get(uid);
    }

    // Удаление по UID

    public void deleteTaskByUid(int uid) {

        tasksList.remove(uid);
    }

    public void deleteEpicByUid(int uid) {

        Epic epic = epicsList.remove(uid);
        if (epic != null) {
            for (Integer subUid : epic.getSubTaskList()) {
                subTasksList.remove(subUid);
            }
        }
    }


    public void deleteSubTaskByUid(int uid) {
        Subtask subtask = subTasksList.remove(uid);
        if (subtask != null) {
            Epic epic = epicsList.get(subtask.getEpicUid());
            if (epic != null) {
                epic.removeSubTaskList(uid);
                updateEpicStatus(epic);
            }
        }
    }

    // Удаление


    public void removeAllTasks() {
        tasksList.clear();
    }

    public void removeAllEpics() {
        if (!epicsList.isEmpty()) {
            epicsList.clear();
            subTasksList.clear();
        }
    }

    public void removeAllSubTasks() {
        if (!subTasksList.isEmpty()) {
            subTasksList.clear();
            for (Epic epic : epicsList.values()) {
                epic.cleanSubTaskList();
                epic.setStatus(Status.NEW);
            }
        }
    }

    // Обновление статуса эпика

    private void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubTaskList();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }


        boolean allDone = true;
        boolean allNew = true;

        for (Integer id : subtaskIds) {
            if (subTasksList.get(id) == null) {
                continue;
            }
            if (subTasksList.get(id).getStatus() != Status.DONE) {
                allDone = false;

            }
            if (subTasksList.get(id).getStatus() != Status.NEW) {
                allNew = false;
            }
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }

    }


}