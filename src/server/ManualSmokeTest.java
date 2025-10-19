package server;

import com.google.gson.Gson;
import util.InMemoryTaskManager;
import tasks.Task;
import tasks.TaskStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

public class ManualSmokeTest {
    public static void main(String[] args) {
        try {
            System.out.println("ManualSmokeTest: starting");
            InMemoryTaskManager manager = new InMemoryTaskManager();
            HttpTaskServer server = new HttpTaskServer(manager);

            // start server
            System.out.println("ManualSmokeTest: starting server...");
            server.start();

            // Используем общий Gson, настроенный в сервере (включает адаптеры LocalDateTime и Duration)
            Gson gson = HttpTaskServer.getGson();

            // create a task using available constructor and setters
            Task task = new Task();
            task.setName("Smoke Task");
            task.setDescription("Created by smoke test");
            task.setStatus(TaskStatus.NEW);
            task.setDuration(Duration.ofMinutes(30));
            task.setStartTime(LocalDateTime.now().plusHours(1));

            String json = gson.toJson(task);

            System.out.println("ManualSmokeTest: sending POST to /tasks with body: " + json);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/tasks"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("ManualSmokeTest: Response status: " + response.statusCode());
            System.out.println("ManualSmokeTest: Response body: " + response.body());

            // stop server
            System.out.println("ManualSmokeTest: stopping server...");
            server.stop();
            System.out.println("ManualSmokeTest: done");
        } catch (Exception e) {
            System.err.println("ManualSmokeTest: exception");
            e.printStackTrace();
        }
    }
}