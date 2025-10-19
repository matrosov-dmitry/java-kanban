package util;

import java.io.File;
import java.io.IOException;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    @Override
    protected FileBackedTaskManager createManager() {
        try {
            File tempFile = File.createTempFile("task_manager", ".csv");
            return new FileBackedTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // Здесь могут быть специфические тесты только для FileBackedTaskManager
}