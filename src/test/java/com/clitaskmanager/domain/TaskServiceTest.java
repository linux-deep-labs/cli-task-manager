package com.clitaskmanager.domain;

import com.clitaskmanager.domain.model.Task;
import com.clitaskmanager.domain.model.TaskPriority;
import com.clitaskmanager.domain.model.TaskStatus;
import com.clitaskmanager.domain.service.TaskService;
import com.clitaskmanager.persistence.api.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskServiceTest {

    private TaskService taskService;
    private MockTaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MockTaskRepository();
        taskService = new TaskService(repository);
    }

    @Test
    void testCreateTask() {
        Task created = taskService.createTask("Test Title", "Test Desc", TaskPriority.HIGH, LocalDate.now().plusDays(1));

        assertThat(created.id()).isEqualTo(1L);
        assertThat(created.title()).isEqualTo("Test Title");
        assertThat(created.status()).isEqualTo(TaskStatus.PENDING);
        assertThat(created.priority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void testCreateTaskValidation() {
        assertThatThrownBy(() -> taskService.createTask("", "Desc", TaskPriority.LOW, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCompleteTask() {
        Task task = taskService.createTask("Complete me", null, TaskPriority.MEDIUM, null);
        Task completed = taskService.completeTask(task.id());

        assertThat(completed.status()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(completed.completedAt()).isNotNull();
    }

    private static class MockTaskRepository implements TaskRepository {
        private final List<Task> tasks = new ArrayList<>();
        private long idCounter = 1;

        @Override
        public Task save(Task task) {
            Task saved = new Task(idCounter++, task.title(), task.description(), task.status(), task.priority(), task.dueDate(), task.createdAt(), task.completedAt(), task.totalFocusSeconds());
            tasks.add(saved);
            return saved;
        }

        @Override
        public Optional<Task> findById(long id) {
            return tasks.stream().filter(t -> t.id() == id).findFirst();
        }

        @Override
        public List<Task> findAll(TaskStatus statusFilter, TaskPriority priorityFilter) {
            return tasks.stream()
                .filter(t -> statusFilter == null || t.status() == statusFilter)
                .filter(t -> priorityFilter == null || t.priority() == priorityFilter)
                .toList();
        }

        @Override
        public boolean update(Task task) {
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).id().equals(task.id())) {
                    tasks.set(i, task);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean deleteById(long id) {
            return tasks.removeIf(t -> t.id() == id);
        }
    }
}
