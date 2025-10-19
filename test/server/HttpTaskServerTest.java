package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;
import util.InMemoryTaskManager;
import util.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {

    private HttpTaskServer server;
    private HttpClient client;
    private Gson gson;
    private InMemoryTaskManager taskManager;

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        server = new HttpTaskServer(taskManager);
        client = HttpClient.newHttpClient();
        // Используем общий Gson, настроенный в сервере (включает адаптеры LocalDateTime и Duration)
        gson = HttpTaskServer.getGson();

        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testCreateTask() throws IOException, InterruptedException {
        // Создаем задачу для отправки
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                           60, LocalDateTime.now().plusHours(1));

        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        // Проверяем, что задача добавилась в менеджер
        assertEquals(1, taskManager.getAllTasks().size());
        Task createdTask = taskManager.getAllTasks().get(0);
        assertEquals("Test Task", createdTask.getName());
        assertEquals("Test Description", createdTask.getDescription());
    }

    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        // Добавляем задачу напрямую в менеджер
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                           60, LocalDateTime.now().plusHours(1));
        taskManager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, tasks.length);
        assertEquals("Test Task", tasks[0].getName());
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        // Добавляем задачу напрямую в менеджер
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                           60, LocalDateTime.now().plusHours(1));
        taskManager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task receivedTask = gson.fromJson(response.body(), Task.class);
        assertEquals("Test Task", receivedTask.getName());
        assertEquals(1, receivedTask.getId());
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        // Добавляем задачу напрямую в менеджер
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                           60, LocalDateTime.now().plusHours(1));
        taskManager.createTask(task);

        assertEquals(1, taskManager.getAllTasks().size());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, taskManager.getAllTasks().size());
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        // Добавляем задачу напрямую в менеджер
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                           60, LocalDateTime.now().plusHours(1));
        taskManager.createTask(task);

        // Обновляем задачу
        Task updatedTask = new Task("Updated Task", "Updated Description", TaskStatus.IN_PROGRESS,
                                  90, LocalDateTime.now().plusHours(2));
        updatedTask.setId(1);

        String taskJson = gson.toJson(updatedTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task taskFromManager = taskManager.getAllTasks().get(0);
        assertEquals("Updated Task", taskFromManager.getName());
        assertEquals("Updated Description", taskFromManager.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, taskFromManager.getStatus());
    }
}