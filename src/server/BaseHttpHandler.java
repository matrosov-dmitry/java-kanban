package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import util.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler implements HttpHandler {

    // Используем общий Gson, настроенный в HttpTaskServer для корректной обработки LocalDateTime и Duration
    protected final Gson gson = HttpTaskServer.getGson();
    protected final TaskManager taskManager;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET":
                handleGet(exchange, path);
                break;
            case "POST":
                handlePost(exchange, path);
                break;
            case "DELETE":
                handleDelete(exchange, path);
                break;
            default:
                sendNotFound(exchange, "Метод не поддерживается");
        }
    }

    protected void handleGet(HttpExchange exchange, String path) throws IOException {
        sendNotFound(exchange, "GET метод не реализован в базовом классе");
    }

    protected void handlePost(HttpExchange exchange, String path) throws IOException {
        sendNotFound(exchange, "POST метод не реализован в базовом классе");
    }

    protected void handleDelete(HttpExchange exchange, String path) throws IOException {
        sendNotFound(exchange, "DELETE метод не реализован в базовом классе");
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    // Вспомогательный метод для отправки HTTP 201 Created ответов с JSON телом
    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(201, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        byte[] response = gson.toJson(new ErrorResponse(message)).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(404, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
        byte[] response = gson.toJson(new ErrorResponse(message)).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(406, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendInternalError(HttpExchange exchange, String message) throws IOException {
        byte[] response = gson.toJson(new ErrorResponse(message)).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(500, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}