package server;

import com.sun.net.httpserver.HttpExchange;
import tasks.Epic;
import util.TaskManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        try {
            if (pathParts.length == 2) { // /epics
                ArrayList<Epic> epics = taskManager.getAllEpics();
                String response = gson.toJson(epics);
                sendText(exchange, response);
            } else if (pathParts.length == 3) { // /epics/{id}
                int id = Integer.parseInt(pathParts[2]);
                Optional<Epic> epic = taskManager.getEpicById(id);
                if (epic.isPresent()) {
                    String response = gson.toJson(epic.get());
                    sendText(exchange, response);
                } else {
                    sendNotFound(exchange, "Эпик с id=" + id + " не найден");
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

            if (pathParts.length == 2) { // /epics - создание нового эпика
                Epic epic = gson.fromJson(requestBody, Epic.class);
                if (epic.getName() == null || epic.getDescription() == null) {
                    sendNotFound(exchange, "Название и описание эпика обязательны");
                    return;
                }

                Epic createdEpic = taskManager.createEpic(epic);
                String response = gson.toJson(createdEpic);
                sendCreated(exchange, response);

            } else if (pathParts.length == 3) { // /epics/{id} - обновление эпика
                int id = Integer.parseInt(pathParts[2]);
                Epic epicUpdate = gson.fromJson(requestBody, Epic.class);

                Optional<Epic> existingEpic = taskManager.getEpicById(id);
                if (existingEpic.isEmpty()) {
                    sendNotFound(exchange, "Эпик с id=" + id + " не найден");
                    return;
                }

                Epic updatedEpic = new Epic(id, epicUpdate.getName(), epicUpdate.getDescription());
                updatedEpic.setStatus(epicUpdate.getStatus() != null ? epicUpdate.getStatus() : existingEpic.get().getStatus());

                taskManager.updateEpic(updatedEpic);
                String response = gson.toJson(updatedEpic);
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
            if (pathParts.length == 2) { // /epics - удалить все эпики
                taskManager.clearAllEpics();
                sendText(exchange, "Все эпики удалены");
            } else if (pathParts.length == 3) { // /epics/{id} - удалить эпик по id
                int id = Integer.parseInt(pathParts[2]);
                Optional<Epic> epic = taskManager.getEpicById(id);
                if (epic.isPresent()) {
                    taskManager.deleteEpicById(id);
                    sendText(exchange, "Эпик с id=" + id + " удален");
                } else {
                    sendNotFound(exchange, "Эпик с id=" + id + " не найден");
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
}