package server;

import com.sun.net.httpserver.HttpExchange;
import tasks.Task;
import util.TaskManager;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(HttpExchange exchange, String path) throws IOException {
        try {
            List<Task> history = taskManager.getHistory();
            String response = gson.toJson(history);
            sendText(exchange, response);
        } catch (Exception e) {
            sendInternalError(exchange, "Внутренняя ошибка сервера");
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange, String path) throws IOException {
        sendNotFound(exchange, "POST метод не поддерживается для истории");
    }

    @Override
    protected void handleDelete(HttpExchange exchange, String path) throws IOException {
        sendNotFound(exchange, "DELETE метод не поддерживается для истории");
    }
}