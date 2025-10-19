package server;

import com.sun.net.httpserver.HttpServer;
import util.Managers;
import util.TaskManager;
import util.LocalDateTimeAdapter;
import util.DurationAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private final HttpServer httpServer;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Настройка маршрутизации
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    // Конструктор по умолчанию для обратной совместимости
    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP сервер запущен на порту " + PORT);
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("HTTP сервер остановлен");
    }

    // Общий экземпляр Gson, настроенный для LocalDateTime и Duration
    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer();
            server.start();

            // Добавляем shutdown hook для корректного завершения работы сервера
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Завершение работы сервера...");
                server.stop();
            }));

        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
        }
    }
}