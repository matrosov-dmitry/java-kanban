import task.*;

public class Main {
    public static void main(String[] args) {
        Manager manager = new Manager();

        Task taskMarket = manager.createTask(new Task("Магазин", "Купить продукты", Status.NEW));
        Task taskWork = manager.createTask(new Task("Работа", "Проверить почту", Status.NEW));

        Epic epicPracticum = manager.createEpic(new Epic("Спринт 4", "Создание проекта"));
        Subtask subtaskGit = manager.addSubTask(new Subtask("Git", "Подключить Git", Status.NEW, epicPracticum.getUid()));
        Subtask subtaskProject = manager.addSubTask(new Subtask("Project", "Создать проект", Status.NEW, epicPracticum.getUid()));

        Epic epicRepairs = manager.createEpic(new Epic("Ремонт", "Создание проекта"));
        Subtask subtaskMaterials = manager.addSubTask(new Subtask("Материалы", "Купить материалы", Status.NEW, epicRepairs.getUid()));

        System.out.println("=== Печать списков задач ============");
        System.out.println(taskMarket);
        System.out.println(taskWork);
        System.out.println();

        System.out.println("=== Печать эпика 1 ==================");
        System.out.println(epicPracticum);
        System.out.println(subtaskGit);
        System.out.println(subtaskProject);
        System.out.println();

        System.out.println("=== Печать эпика 2 ==================");
        System.out.println(epicRepairs);
        System.out.println(subtaskMaterials);
        System.out.println();

        System.out.println("=== Обновление задачи taskMarket ====");
        Task updatedTask = new Task(taskMarket);
        System.out.println(taskMarket);
        updatedTask.setName("Магазин лента");
        updatedTask.setStatus(Status.IN_PROGRESS);
        manager.updateTask(updatedTask);
        System.out.println(taskMarket);
        System.out.println();

        System.out.println("==== Обновление эпика epicPracticum ===");
        Epic updatedEpic = new Epic(epicPracticum);
        System.out.println(epicPracticum);
        updatedEpic.setName("Спринт 5");
        manager.updateEpic(updatedEpic);
        System.out.println(updatedEpic);
        System.out.println();

        System.out.println("=== Обновление подзадачи subtaskMaterials =");
        Subtask updatedSubtask = new Subtask(subtaskMaterials);
        updatedSubtask.setStatus(Status.DONE);
        subtaskMaterials = manager.updateSubtask(updatedSubtask);
        epicRepairs = manager.updateEpicStatus(epicRepairs);
        System.out.println(subtaskMaterials);
        System.out.println(epicRepairs);
        System.out.println();

        System.out.println("===Удаление по UID =====================");
        manager.printAllTasks();
        System.out.println("Удаление taskMarket");
        manager.deleteTaskByUid(taskMarket.getUid());
        manager.printAllTasks();
        System.out.println();
        manager.printAllEpics();
        System.out.println("Удаление epicPracticum");
        manager.deleteEpicByUid(epicPracticum.getUid());
        manager.printAllEpics();


    }
}