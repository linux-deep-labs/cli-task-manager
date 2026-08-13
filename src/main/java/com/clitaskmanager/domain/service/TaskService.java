package com.clitaskmanager.domain.service;

import com.clitaskmanager.domain.model.Task;
import com.clitaskmanager.domain.model.TaskPriority;
import com.clitaskmanager.domain.model.TaskStatus;
import com.clitaskmanager.persistence.api.TaskRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(String title, String description, TaskPriority priority, LocalDate dueDate) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title is required");
        }
        Task task = Task.createNew(title, description, priority, dueDate);
        return taskRepository.save(task);
    }

    public Optional<Task> getTaskById(long id) {
        return taskRepository.findById(id);
    }

    public List<Task> listTasks(TaskStatus statusFilter, TaskPriority priorityFilter) {
        return taskRepository.findAll(statusFilter, priorityFilter);
    }

    public Task updateTask(long id, String newTitle, String newDescription, TaskPriority newPriority, LocalDate newDueDate) {
        Task existing = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task #" + id + " not found"));

        String title = (newTitle != null && !newTitle.isBlank()) ? newTitle : existing.title();
        String description = newDescription != null ? newDescription : existing.description();
        TaskPriority priority = newPriority != null ? newPriority : existing.priority();
        LocalDate dueDate = newDueDate != null ? newDueDate : existing.dueDate();

        Task updated = new Task(
            existing.id(),
            title,
            description,
            existing.status(),
            priority,
            dueDate,
            existing.createdAt(),
            existing.completedAt(),
            existing.totalFocusSeconds()
        );

        taskRepository.update(updated);
        return updated;
    }

    public Task completeTask(long id) {
        Task existing = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task #" + id + " not found"));

        if (existing.status() == TaskStatus.COMPLETED) {
            return existing;
        }

        Task completed = new Task(
            existing.id(),
            existing.title(),
            existing.description(),
            TaskStatus.COMPLETED,
            existing.priority(),
            existing.dueDate(),
            existing.createdAt(),
            Instant.now(),
            existing.totalFocusSeconds()
        );

        taskRepository.update(completed);
        return completed;
    }

    public boolean deleteTask(long id) {
        if (taskRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Task #" + id + " not found");
        }
        return taskRepository.deleteById(id);
    }
}
