package service;

import model.Task;
import model.Epic;
import model.Subtask;
import java.util.List;

public interface TaskManager {
    void addTask(Task task);
    
    void addSubtask(Subtask subtask);
    
    void removeTask(int id);
    
    void removeSubtask(int id);
    
    void updateTask(Task task);
    
    void updateSubtask(Subtask subtask);
    
    Task getTask(int id);
    
    Epic getEpic(int id);
    
    Subtask getSubtask(int id);
    
    List<Task> getPrioritizedTasks();
    
    List<Subtask> getSubtasksOfEpic(int epicId);
}