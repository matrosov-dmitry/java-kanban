import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import util.Managers;
import util.TaskManager;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Запуск тестирования истории задач
        testHistoryManager();
    }

    /**
     * Метод для тестирования функциональности менеджера истории
     */
    public static void testHistoryManager() {
        System.out.println("Начинаем тестирование менеджера истории задач");
        System.out.println("==============================================");

        TaskManager taskManager = Managers.getDefault();

        // 1. Создаем две задачи, эпик с тремя подзадачами и эпик без подзадач
        System.out.println("1. Создание задач");

        // Создаем две обычные задачи
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание задачи 1"));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание задачи 2"));

        // Создаем эпик с тремя подзадачами
        Epic epicWithSubtasks = taskManager.createEpic(new Epic("Эпик с подзадачами", "Описание эпика с подзадачами"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", epicWithSubtasks.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", epicWithSubtasks.getId()));
        Subtask subtask3 = taskManager.createSubtask(new Subtask("Подзадача 3", "Описание подзадачи 3", epicWithSubtasks.getId()));

        // Создаем эпик без подзадач
        Epic epicWithoutSubtasks = taskManager.createEpic(new Epic("Эпик без подзадач", "Описание эпика без подзадач"));

        System.out.println("Созданы задачи:");
        System.out.println("- Задача 1 (ID: " + task1.getId() + ")");
        System.out.println("- Задача 2 (ID: " + task2.getId() + ")");
        System.out.println("- Эпик с подзадачами (ID: " + epicWithSubtasks.getId() + ")");
        System.out.println("  - Подзадача 1 (ID: " + subtask1.getId() + ")");
        System.out.println("  - Подзадача 2 (ID: " + subtask2.getId() + ")");
        System.out.println("  - Подзадача 3 (ID: " + subtask3.getId() + ")");
        System.out.println("- Эпик без подзадач (ID: " + epicWithoutSubtasks.getId() + ")");

        System.out.println("\n2. Запрашиваем задачи в разном порядке и проверяем историю");

        // Первый запрос задач
        System.out.println("\nЗапрос #1: task1, epicWithSubtasks, subtask1");
        taskManager.getTaskById(task1.getId());
        taskManager.getEpicById(epicWithSubtasks.getId());
        taskManager.getSubtaskById(subtask1.getId());

        printHistory(taskManager);

        // Второй запрос задач
        System.out.println("\nЗапрос #2: task2, subtask2, epicWithoutSubtasks");
        taskManager.getTaskById(task2.getId());
        taskManager.getSubtaskById(subtask2.getId());
        taskManager.getEpicById(epicWithoutSubtasks.getId());

        printHistory(taskManager);

        // Третий запрос задач с повторами
        System.out.println("\nЗапрос #3: subtask3, task1 (повтор), epicWithSubtasks (повтор)");
        taskManager.getSubtaskById(subtask3.getId());
        taskManager.getTaskById(task1.getId());
        taskManager.getEpicById(epicWithSubtasks.getId());

        printHistory(taskManager);

        System.out.println("\n3. Удаляем задачу из истории");
        System.out.println("Удаляем задачу 2 (ID: " + task2.getId() + ")");
        taskManager.deleteTaskById(task2.getId());

        System.out.println("История после удаления задачи 2:");
        printHistory(taskManager);

        System.out.println("\n4. Удаляем эпик с подзадачами");
        System.out.println("Удаляем эпик с подзадачами (ID: " + epicWithSubtasks.getId() + ")");
        taskManager.deleteEpicById(epicWithSubtasks.getId());

        System.out.println("История после удаления эпика с подзадачами:");
        printHistory(taskManager);

        System.out.println("\nТестирование менеджера истории задач завершено");
    }

    /**
     * Вспомогательный метод для вывода истории задач
     */
    private static void printHistory(TaskManager taskManager) {
        List<Task> history = taskManager.getHistory();
        System.out.println("История просмотров (" + history.size() + " задач):");

        if (history.isEmpty()) {
            System.out.println("История пуста");
        } else {
            for (int i = 0; i < history.size(); i++) {
                Task task = history.get(i);
                String taskType = "Задача";
                if (task instanceof Epic) {
                    taskType = "Эпик";
                } else if (task instanceof Subtask) {
                    taskType = "Подзадача";
                }
                System.out.println((i + 1) + ". " + taskType + ": " + task.getName() + " (ID: " + task.getId() + ")");
            }
        }
    }

    /**
     * Метод для вывода всех задач в менеджере
     */
    private static void printAllTasks(TaskManager taskManager) {
        System.out.println("\nВсе задачи:");
        System.out.println("Обычные задачи:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println("- " + task.getName() + " (ID: " + task.getId() + ")");
        }

        System.out.println("\nЭпики:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println("- " + epic.getName() + " (ID: " + epic.getId() + ")");
            System.out.println("  Подзадачи эпика:");
            for (Subtask subtask : taskManager.getSubtasksOfEpic(epic.getId())) {
                System.out.println("  - " + subtask.getName() + " (ID: " + subtask.getId() + ")");
            }
        }
    }
}