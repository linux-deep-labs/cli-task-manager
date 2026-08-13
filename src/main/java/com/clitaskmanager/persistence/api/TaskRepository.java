package com.clitaskmanager.persistence.api;

import com.clitaskmanager.domain.model.Task;
import com.clitaskmanager.domain.model.TaskPriority;
import com.clitaskmanager.domain.model.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(long id);
    List<Task> findAll(TaskStatus statusFilter, TaskPriority priorityFilter);
    boolean update(Task task);
    boolean deleteById(long id);
}
