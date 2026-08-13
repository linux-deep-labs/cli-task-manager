package com.clitaskmanager.persistence;

import com.clitaskmanager.domain.model.FocusSession;
import com.clitaskmanager.domain.model.Task;
import com.clitaskmanager.domain.model.TaskPriority;
import com.clitaskmanager.domain.model.TaskStatus;
import com.clitaskmanager.domain.model.TimerStatus;
import com.clitaskmanager.persistence.sqlite.DatabaseConnectionManager;
import com.clitaskmanager.persistence.sqlite.SqliteFocusSessionRepository;
import com.clitaskmanager.persistence.sqlite.SqliteTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SqliteRepositoryIntegrationTest {

    private SqliteTaskRepository taskRepository;
    private SqliteFocusSessionRepository focusSessionRepository;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws Exception {
        File dbFile = tempDir.resolve("test-tasks.db").toFile();
        DatabaseConnectionManager.setDbPathOverride(dbFile.getAbsolutePath());
        DatabaseConnectionManager.initializeDatabase();

        taskRepository = new SqliteTaskRepository();
        focusSessionRepository = new SqliteFocusSessionRepository();
    }

    @Test
    void testTaskCrudOperations() {
        // Create
        Task newTask = Task.createNew("Fix authentication bug", "OAuth token validation error", TaskPriority.HIGH, LocalDate.now().plusDays(3));
        Task saved = taskRepository.save(newTask);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.title()).isEqualTo("Fix authentication bug");
        assertThat(saved.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(saved.status()).isEqualTo(TaskStatus.PENDING);

        // Read
        Optional<Task> found = taskRepository.findById(saved.id());
        assertThat(found).isPresent();
        assertThat(found.get().title()).isEqualTo("Fix authentication bug");

        // Update
        Task updatedTask = new Task(
            saved.id(),
            "Fix authentication bug v2",
            saved.description(),
            TaskStatus.COMPLETED,
            saved.priority(),
            saved.dueDate(),
            saved.createdAt(),
            Instant.now(),
            saved.totalFocusSeconds()
        );
        boolean updated = taskRepository.update(updatedTask);
        assertThat(updated).isTrue();

        Optional<Task> refetched = taskRepository.findById(saved.id());
        assertThat(refetched).isPresent();
        assertThat(refetched.get().status()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(refetched.get().title()).isEqualTo("Fix authentication bug v2");

        // List
        List<Task> completedTasks = taskRepository.findAll(TaskStatus.COMPLETED, null);
        assertThat(completedTasks).hasSize(1);

        // Delete
        boolean deleted = taskRepository.deleteById(saved.id());
        assertThat(deleted).isTrue();
        assertThat(taskRepository.findById(saved.id())).isEmpty();
    }

    @Test
    void testFocusSessionPersistence() {
        Task task = taskRepository.save(Task.createNew("Task for timer", "Description", TaskPriority.MEDIUM, null));

        FocusSession session = FocusSession.createNew(task.id(), 25);
        FocusSession savedSession = focusSessionRepository.save(session);

        assertThat(savedSession.id()).isNotNull();
        assertThat(savedSession.status()).isEqualTo(TimerStatus.ACTIVE);

        Optional<FocusSession> active = focusSessionRepository.findActiveSession();
        assertThat(active).isPresent();
        assertThat(active.get().id()).isEqualTo(savedSession.id());

        FocusSession completedSession = new FocusSession(
            savedSession.id(),
            savedSession.taskId(),
            savedSession.plannedDurationMinutes(),
            1500L, // 25 minutes = 1500 seconds
            savedSession.startedAt(),
            Instant.now(),
            TimerStatus.COMPLETED
        );
        focusSessionRepository.update(completedSession);

        assertThat(focusSessionRepository.findActiveSession()).isEmpty();

        long totalFocus = focusSessionRepository.calculateTotalFocusSecondsForTask(task.id());
        assertThat(totalFocus).isEqualTo(1500L);
    }
}
