package server;

import com.sun.net.httpserver.HttpExchange;
import tasks.Subtask;
import util.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        try {
            if (pathParts.length == 2) { // /subtasks
                ArrayList<Subtask> subtasks = taskManager.getAllSubtasks();
                String response = gson.toJson(subtasks);
                sendText(exchange, response);
            } else if (pathParts.length == 3) {
                // Может быть /subtasks/{id} или /subtasks/epic
                if ("epic".equals(pathParts[2])) {
                    // parse query param id
                    String query = exchange.getRequestURI().getQuery();
                    if (query == null || !query.startsWith("id=")) {
                        sendNotFound(exchange, "Параметр id обязателен");
                        return;
                    }
                    try {
                        int epicId = Integer.parseInt(query.substring(3));
                        ArrayList<Subtask> subtasks = taskManager.getSubtasksOfEpic(epicId);
                        String response = gson.toJson(subtasks);
                        sendText(exchange, response);
                    } catch (NumberFormatException e) {
                        sendNotFound(exchange, "Неверный формат id");
                    }
                } else {
                    // /subtasks/{id}
                    int id = Integer.parseInt(pathParts[2]);
                    Optional<Subtask> subtask = taskManager.getSubtaskById(id);
                    if (subtask.isPresent()) {
                        String response = gson.toJson(subtask.get());
                        sendText(exchange, response);
                    } else {
                        sendNotFound(exchange, "Подзадача с id=" + id + " не найдена");
                    }
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

            if (pathParts.length == 2) { // /subtasks - создание новой подзадачи
                Subtask subtask = gson.fromJson(requestBody, Subtask.class);
                if (subtask.getName() == null || subtask.getDescription() == null || subtask.getEpicId() == 0) {
                    sendNotFound(exchange, "Название, описание и epicId подзадачи обязательны");
                    return;
                }

                // Проверка существования эпика
                Optional<tasks.Epic> epic = taskManager.getEpicById(subtask.getEpicId());
                if (epic.isEmpty()) {
                    sendNotFound(exchange, "Эпик с id=" + subtask.getEpicId() + " не найден");
                    return;
                }

                // Проверка на пересечения с существующими задачами
                if (subtask.getStartTime() != null && subtask.getDuration() != null) {
                    if (taskManager.getPrioritizedTasks().stream()
                            .anyMatch(existingTask -> areTasksOverlapping(subtask, existingTask))) {
                        sendHasInteractions(exchange, "Подзадача пересекается с существующими задачами");
                        return;
                    }
                }

                Subtask createdSubtask = taskManager.createSubtask(subtask);
                String response = gson.toJson(createdSubtask);
                sendCreated(exchange, response);

            } else if (pathParts.length == 3) { // /subtasks/{id} - обновление подзадачи
                int id = Integer.parseInt(pathParts[2]);
                Subtask subtaskUpdate = gson.fromJson(requestBody, Subtask.class);

                Optional<Subtask> existingSubtask = taskManager.getSubtaskById(id);
                if (existingSubtask.isEmpty()) {
                    sendNotFound(exchange, "Подзадача с id=" + id + " не найдена");
                    return;
                }

                // Проверка существования эпика при обновлении
                if (subtaskUpdate.getEpicId() != 0) {
                    Optional<tasks.Epic> epic = taskManager.getEpicById(subtaskUpdate.getEpicId());
                    if (epic.isEmpty()) {
                        sendNotFound(exchange, "Эпик с id=" + subtaskUpdate.getEpicId() + " не найден");
                        return;
                    }
                }

                Subtask updatedSubtask = new Subtask(id, subtaskUpdate.getName(), subtaskUpdate.getDescription(),
                        subtaskUpdate.getStatus() != null ? subtaskUpdate.getStatus() : existingSubtask.get().getStatus(),
                        subtaskUpdate.getEpicId() != 0 ? subtaskUpdate.getEpicId() : existingSubtask.get().getEpicId(),
                        subtaskUpdate.getDuration() != null ? subtaskUpdate.getDuration() : existingSubtask.get().getDuration(),
                        subtaskUpdate.getStartTime() != null ? subtaskUpdate.getStartTime() : existingSubtask.get().getStartTime());

                // Проверка на пересечения при обновлении
                if (updatedSubtask.getStartTime() != null && updatedSubtask.getDuration() != null) {
                    if (taskManager.getPrioritizedTasks().stream()
                            .filter(t -> t.getId() != id) // Исключаем текущую подзадачу
                            .anyMatch(existingTask -> areTasksOverlapping(updatedSubtask, existingTask))) {
                        sendHasInteractions(exchange, "Подзадача пересекается с существующими задачами");
                        return;
                    }
                }

                taskManager.updateSubtask(updatedSubtask);
                String response = gson.toJson(updatedSubtask);
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
            if (pathParts.length == 2) { // /subtasks - удалить все подзадачи
                taskManager.deleteAllSubtasks();
                sendText(exchange, "Все подзадачи удалены");
            } else if (pathParts.length == 3) { // /subtasks/{id} - удалить подзадачу по id
                int id = Integer.parseInt(pathParts[2]);
                Optional<Subtask> subtask = taskManager.getSubtaskById(id);
                if (subtask.isPresent()) {
                    taskManager.deleteSubtaskById(id);
                    sendText(exchange, "Подзадача с id=" + id + " удалена");
                } else {
                    sendNotFound(exchange, "Подзадача с id=" + id + " не найдена");
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

    private boolean areTasksOverlapping(tasks.Task task1, tasks.Task task2) {
        if (task1.getStartTime() == null || task1.getEndTime() == null ||
            task2.getStartTime() == null || task2.getEndTime() == null) {
            return false;
        }

        // Проверяем, что задачи не заканчиваются в одно время и не начинаются в одно время
        return task1.getStartTime().isBefore(task2.getEndTime()) &&
               task2.getStartTime().isBefore(task1.getEndTime());
    }
}