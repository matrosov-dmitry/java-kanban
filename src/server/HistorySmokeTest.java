package server;

import util.InMemoryTaskManager;
import tasks.Task;
import tasks.Epic;
import tasks.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public class HistorySmokeTest {
    public static void main(String[] args) {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW, 60, LocalDateTime.now().plusHours(1));
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS, 90, LocalDateTime.now().plusHours(2));
        Epic epic = new Epic("Epic", "Epic Description");

        manager.createTask(task1);
        manager.createTask(task2);
        manager.createEpic(epic);

        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getEpicById(3);
        manager.getTaskById(1);

        List<Task> history = manager.getHistory();
        System.out.println("History size: " + history.size());
        System.out.print("History ids: ");
        for (Task t : history) {
            System.out.print(t.getId() + " ");
        }
        System.out.println();
    }
}