package server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
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

public class HttpTaskServerHistoryTest {

    private HttpTaskServer server;
    private HttpClient client;
    private Gson gson;
    private InMemoryTaskManager taskManager;

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        server = new HttpTaskServer(taskManager);
        client = HttpClient.newHttpClient();
        gson = HttpTaskServer.getGson();

        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        // Создаем задачи
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW,
                            60, LocalDateTime.now().plusHours(1));
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS,
                            90, LocalDateTime.now().plusHours(2));
        Epic epic = new Epic("Epic", "Epic Description");

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createEpic(epic);

        // Получаем задачи по ID, чтобы они попали в историю
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getEpicById(3);
        taskManager.getTaskById(1); // Повторно, чтобы проверить порядок

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] historyTasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(3, historyTasks.length);

        // Проверяем порядок: последний просмотренный первым
        assertEquals(1, historyTasks[0].getId()); // Task 1 был просмотрен последним
        assertEquals(3, historyTasks[1].getId()); // Epic
        assertEquals(2, historyTasks[2].getId()); // Task 2
    }

    @Test
    public void testEmptyHistory() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] historyTasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, historyTasks.length);
    }
}