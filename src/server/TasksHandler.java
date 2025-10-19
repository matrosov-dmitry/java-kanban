package server;

import com.sun.net.httpserver.HttpExchange;
import tasks.Task;
import tasks.TaskStatus;
import util.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        try {
            if (pathParts.length == 2) { // /tasks
                ArrayList<Task> tasks = taskManager.getAllTasks();
                String response = gson.toJson(tasks);
                sendText(exchange, response);
            } else if (pathParts.length == 3) { // /tasks/{id}
                int id = Integer.parseInt(pathParts[2]);
                Optional<Task> task = taskManager.getTaskById(id);
                if (task.isPresent()) {
                    String response = gson.toJson(task.get());
                    sendText(exchange, response);
                } else {
                    sendNotFound(exchange, "Задача с id=" + id + " не найдена");
                }
            } else {
                sendNotFound(exchange, "Неверный путь запроса");
            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Неверный формат id");
        } catch (Exception e) {
            sendInternalError(exchange, "Внутренняя ошибка сервера");
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        try {
            String requestBody = readRequestBody(exchange);

            if (pathParts.length == 2) { // /tasks - создание новой задачи
                Task task = gson.fromJson(requestBody, Task.class);
                if (task.getName() == null || task.getDescription() == null) {
                    sendNotFound(exchange, "Название и описание задачи обязательны");
                    return;
                }

                // Проверка на пересечения с существующими задачами
                if (task.getStartTime() != null && task.getDuration() != null) {
                    if (taskManager.getPrioritizedTasks().stream()
                            .anyMatch(existingTask -> areTasksOverlapping(task, existingTask))) {
                        sendHasInteractions(exchange, "Задача пересекается с существующими задачами");
                        return;
                    }
                }

                Task createdTask = taskManager.createTask(task);
                String response = gson.toJson(createdTask);
                sendCreated(exchange, response);

            } else if (pathParts.length == 3) { // /tasks/{id} - обновление задачи
                int id = Integer.parseInt(pathParts[2]);
                Task taskUpdate = gson.fromJson(requestBody, Task.class);

                Optional<Task> existingTask = taskManager.getTaskById(id);
                if (existingTask.isEmpty()) {
                    sendNotFound(exchange, "Задача с id=" + id + " не найдена");
                    return;
                }

                Task updatedTask = new Task(id, taskUpdate.getName(), taskUpdate.getDescription(),
                        taskUpdate.getStatus() != null ? taskUpdate.getStatus() : existingTask.get().getStatus(),
                        taskUpdate.getDuration() != null ? taskUpdate.getDuration() : existingTask.get().getDuration(),
                        taskUpdate.getStartTime() != null ? taskUpdate.getStartTime() : existingTask.get().getStartTime());

                // Проверка на пересечения при обновлении
                if (updatedTask.getStartTime() != null && updatedTask.getDuration() != null) {
                    if (taskManager.getPrioritizedTasks().stream()
                            .filter(t -> t.getId() != id) // Исключаем текущую задачу
                            .anyMatch(task -> areTasksOverlapping(updatedTask, task))) {
                        sendHasInteractions(exchange, "Задача пересекается с существующими задачами");
                        return;
                    }
                }

                taskManager.updateTask(updatedTask);
                String response = gson.toJson(updatedTask);
                sendText(exchange, response);

            } else {
                sendNotFound(exchange, "Неверный путь запроса");
            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Неверный формат id");
        } catch (Exception e) {
            sendInternalError(exchange, "Внутренняя ошибка сервера");
        }
    }

    @Override
    protected void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        try {
            if (pathParts.length == 2) { // /tasks - удалить все задачи
                taskManager.clearAllTasks();
                sendText(exchange, "Все задачи удалены");
            } else if (pathParts.length == 3) { // /tasks/{id} - удалить задачу по id
                int id = Integer.parseInt(pathParts[2]);
                Optional<Task> task = taskManager.getTaskById(id);
                if (task.isPresent()) {
                    taskManager.deleteTaskById(id);
                    sendText(exchange, "Задача с id=" + id + " удалена");
                } else {
                    sendNotFound(exchange, "Задача с id=" + id + " не найдена");
                }
            } else {
                sendNotFound(exchange, "Неверный путь запроса");
            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Неверный формат id");
        } catch (Exception e) {
            sendInternalError(exchange, "Внутренняя ошибка сервера");
        }
    }

    private boolean areTasksOverlapping(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getEndTime() == null ||
            task2.getStartTime() == null || task2.getEndTime() == null) {
            return false;
        }

        // Проверяем, что задачи не заканчиваются в одно время и не начинаются в одно время
        return task1.getStartTime().isBefore(task2.getEndTime()) &&
               task2.getStartTime().isBefore(task1.getEndTime());
    }
}