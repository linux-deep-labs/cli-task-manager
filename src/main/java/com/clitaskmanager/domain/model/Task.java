package com.clitaskmanager.domain.model;

import java.time.Instant;
import java.time.LocalDate;

public record Task(
    Long id,
    String title,
    String description,
    TaskStatus status,
    TaskPriority priority,
    LocalDate dueDate,
    Instant createdAt,
    Instant completedAt,
    long totalFocusSeconds
) {
    public Task {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title cannot be null or blank");
        }
        if (status == null) {
            status = TaskStatus.PENDING;
        }
        if (priority == null) {
            priority = TaskPriority.MEDIUM;
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public static Task createNew(String title, String description, TaskPriority priority, LocalDate dueDate) {
        return new Task(
            null,
            title,
            description,
            TaskStatus.PENDING,
            priority != null ? priority : TaskPriority.MEDIUM,
            dueDate,
            Instant.now(),
            null,
            0L
        );
    }
}
