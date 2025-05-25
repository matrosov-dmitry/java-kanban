package util;

import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    // Хеш-таблица для хранения ссылок на узлы
    private final Map<Integer, Node> nodeMap = new HashMap<>();
    // Указатели на начало и конец списка
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        // Создаем копию задачи для сохранения в истории
        Task savedTask = new Task(task.getName(), task.getDescription());
        savedTask.setStatus(task.getStatus());
        savedTask.setId(task.getId());

        // Удаляем задачу из списка, если она там уже есть
        remove(savedTask.getId());

        // Добавляем задачу в конец списка
        Node node = linkLast(savedTask);

        // Сохраняем ссылку на узел в HashMap
        nodeMap.put(savedTask.getId(), node);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    // Добавляет задачу в конец списка и возвращает созданный узел
    private Node linkLast(Task task) {
        Node newNode = new Node(task);

        if (tail == null) {
            // Список пуст
            head = tail = newNode;
        } else {
            // Добавляем в конец списка
            newNode.prev = tail;
            tail.next = newNode;
            tail = newNode;
        }

        return newNode;
    }

    // Удаляет указанный узел из списка
    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        // Обновляем ссылки соседних узлов
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            // Узел был головой списка
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            // Узел был хвостом списка
            tail = node.prev;
        }

        // Очищаем ссылки в удаляемом узле
        node.next = node.prev = null;
        node.task = null;
    }

    // Собирает все задачи из списка в ArrayList
    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;

        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }

        return tasks;
    }

    // Узел двусвязного списка
    private static class Node {
        Task task;
        Node next;
        Node prev;

        Node(Task task) {
            this.task = task;
        }
    }
}