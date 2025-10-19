package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.TaskStatus;
import util.InMemoryTaskManager;
import util.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerEpicsTest {

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
    public void testCreateEpic() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Epic Description");

        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        // Проверяем, что эпик добавился в менеджер
        assertEquals(1, taskManager.getAllEpics().size());
        Epic createdEpic = taskManager.getAllEpics().get(0);
        assertEquals("Test Epic", createdEpic.getName());
        assertEquals("Epic Description", createdEpic.getDescription());
    }

    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        // Создаем эпики
        Epic epic1 = new Epic("Epic 1", "Description 1");
        Epic epic2 = new Epic("Epic 2", "Description 2");
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(2, epics.length);
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic receivedEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals("Test Epic", receivedEpic.getName());
        assertEquals(1, receivedEpic.getId());
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);

        // Обновляем эпик
        Epic updatedEpic = new Epic("Updated Epic", "Updated Description");
        updatedEpic.setId(1);

        String epicJson = gson.toJson(updatedEpic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/1"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic epicFromManager = taskManager.getAllEpics().get(0);
        assertEquals("Updated Epic", epicFromManager.getName());
        assertEquals("Updated Description", epicFromManager.getDescription());
    }

    @Test
    public void testEpicStatusWithSubtasks() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);

        // Создаем подзадачи
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW,
                                     60, LocalDateTime.now().plusHours(1));
        subtask1.setEpicId(1);
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.NEW,
                                     90, LocalDateTime.now().plusHours(2));
        subtask2.setEpicId(1);
        taskManager.createSubtask(subtask2);

        // Проверяем статус эпика (должен быть NEW, так как все подзадачи NEW)
        Epic epicFromManager = taskManager.getAllEpics().get(0);
        assertEquals(TaskStatus.NEW, epicFromManager.getStatus());

        // Обновляем одну подзадачу на DONE
        Subtask updatedSubtask = new Subtask("Subtask 1", "Description 1", TaskStatus.DONE,
                                           60, LocalDateTime.now().plusHours(1));
        updatedSubtask.setId(2);
        updatedSubtask.setEpicId(1);

        String subtaskJson = gson.toJson(updatedSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/2"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем, что статус эпика изменился на IN_PROGRESS
        Epic updatedEpicFromManager = taskManager.getAllEpics().get(0);
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpicFromManager.getStatus());
    }
}