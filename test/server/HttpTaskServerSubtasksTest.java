package server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.TaskStatus;
import util.InMemoryTaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerSubtasksTest {

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
    public void testCreateSubtask() throws IOException, InterruptedException {
        // Создаем эпик сначала
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);

        // Создаем подзадачу
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", TaskStatus.NEW,
                                    60, LocalDateTime.now().plusHours(1));
        subtask.setEpicId(1);

        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        // Проверяем, что подзадача добавилась в менеджер
        assertEquals(1, taskManager.getAllSubtasks().size());
        Subtask createdSubtask = taskManager.getAllSubtasks().get(0);
        assertEquals("Test Subtask", createdSubtask.getName());
        assertEquals(1, createdSubtask.getEpicId());
    }

    @Test
    public void testGetSubtasksByEpic() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);

        // Создаем подзадачи
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW,
                                     60, LocalDateTime.now().plusHours(1));
        subtask1.setEpicId(1);
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.IN_PROGRESS,
                                     90, LocalDateTime.now().plusHours(2));
        subtask2.setEpicId(1);
        taskManager.createSubtask(subtask2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/epic?id=1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);

        // Создаем подзадачу
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", TaskStatus.NEW,
                                    60, LocalDateTime.now().plusHours(1));
        subtask.setEpicId(1);
        taskManager.createSubtask(subtask);

        // Обновляем подзадачу
        Subtask updatedSubtask = new Subtask("Updated Subtask", "Updated Description", TaskStatus.DONE,
                                           90, LocalDateTime.now().plusHours(2));
        updatedSubtask.setId(2);
        updatedSubtask.setEpicId(1);

        String subtaskJson = gson.toJson(updatedSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/2"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask subtaskFromManager = taskManager.getAllSubtasks().get(0);
        assertEquals("Updated Subtask", subtaskFromManager.getName());
        assertEquals(TaskStatus.DONE, subtaskFromManager.getStatus());
    }
}