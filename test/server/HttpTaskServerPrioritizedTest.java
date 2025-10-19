package server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;
import util.InMemoryTaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerPrioritizedTest {

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
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        // Создаем задачи с разным временем начала
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW,
                            60, LocalDateTime.now().plusHours(4));
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS,
                            90, LocalDateTime.now().plusHours(1));
        Task task3 = new Task("Task 3", "Description 3", TaskStatus.DONE,
                            45, LocalDateTime.now().plusHours(3));

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] prioritizedTasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(3, prioritizedTasks.length);

        // Проверяем сортировку по времени начала
        assertEquals(2, prioritizedTasks[0].getId()); // Task 2 (самая ранняя)
        assertEquals(3, prioritizedTasks[1].getId()); // Task 3
        assertEquals(1, prioritizedTasks[2].getId()); // Task 1 (самая поздняя)
    }

    @Test
    public void testGetPrioritizedTasksEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] prioritizedTasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, prioritizedTasks.length);
    }

    @Test
    public void testGetPrioritizedTasksWithNullStartTime() throws IOException, InterruptedException {
        // Создаем задачу без времени начала
        Task taskWithoutTime = new Task("Task without time", "Description", TaskStatus.NEW);
        taskManager.createTask(taskWithoutTime);

        // Создаем задачу с временем начала
        Task taskWithTime = new Task("Task with time", "Description", TaskStatus.NEW,
                                   60, LocalDateTime.now().plusHours(1));
        taskManager.createTask(taskWithTime);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] prioritizedTasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, prioritizedTasks.length);
        assertEquals("Task with time", prioritizedTasks[0].getName());
    }
}