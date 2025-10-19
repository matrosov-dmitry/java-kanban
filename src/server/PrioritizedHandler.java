package server;

import com.sun.net.httpserver.HttpExchange;
import tasks.Task;
import util.TaskManager;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(HttpExchange exchange, String path) throws IOException {
        try {
            List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
            String response = gson.toJson(prioritizedTasks);
            sendText(exchange, response);
        } catch (Exception e) {
            sendInternalError(exchange, "Внутренняя ошибка сервера");
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange, String path) throws IOException {
        sendNotFound(exchange, "POST метод не поддерживается для приоритетных задач");
    }

    @Override
    protected void handleDelete(HttpExchange exchange, String path) throws IOException {
        sendNotFound(exchange, "DELETE метод не поддерживается для приоритетных задач");
    }
}