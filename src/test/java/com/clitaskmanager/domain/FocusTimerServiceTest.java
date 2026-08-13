package com.clitaskmanager.domain;

import com.clitaskmanager.domain.model.FocusSession;
import com.clitaskmanager.domain.model.Task;
import com.clitaskmanager.domain.model.TaskPriority;
import com.clitaskmanager.domain.model.TaskStatus;
import com.clitaskmanager.domain.model.TimerStatus;
import com.clitaskmanager.domain.service.FocusTimerService;
import com.clitaskmanager.persistence.api.FocusSessionRepository;
import com.clitaskmanager.persistence.api.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FocusTimerServiceTest {

    private FocusTimerService focusTimerService;
    private MockFocusSessionRepository sessionRepository;
    private MockTaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        sessionRepository = new MockFocusSessionRepository();
        taskRepository = new MockTaskRepository();
        focusTimerService = new FocusTimerService(sessionRepository, taskRepository);
    }

    @Test
    void testStartTimer() {
        Task task = taskRepository.save(Task.createNew("Task 1", "Desc", TaskPriority.HIGH, null));
        FocusSession session = focusTimerService.startTimer(task.id(), 25);

        assertThat(session.id()).isNotNull();
        assertThat(session.plannedDurationMinutes()).isEqualTo(25);
        assertThat(session.status()).isEqualTo(TimerStatus.ACTIVE);
    }

    @Test
    void testConcurrentTimerBlocked() {
        focusTimerService.startTimer(null, 25);

        assertThatThrownBy(() -> focusTimerService.startTimer(null, 15))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already running");
    }

    @Test
    void testStopTimer() {
        FocusSession session = focusTimerService.startTimer(null, 25);
        FocusSession stopped = focusTimerService.stopTimer();

        assertThat(stopped.id()).isEqualTo(session.id());
        assertThat(stopped.status()).isEqualTo(TimerStatus.COMPLETED);
        assertThat(stopped.endedAt()).isNotNull();
    }

    private static class MockFocusSessionRepository implements FocusSessionRepository {
        private final List<FocusSession> sessions = new ArrayList<>();
        private long idCounter = 1;

        @Override
        public FocusSession save(FocusSession session) {
            FocusSession saved = new FocusSession(idCounter++, session.taskId(), session.plannedDurationMinutes(), session.actualDurationSeconds(), session.startedAt(), session.endedAt(), session.status());
            sessions.add(saved);
            return saved;
        }

        @Override
        public Optional<FocusSession> findActiveSession() {
            return sessions.stream().filter(s -> s.status() == TimerStatus.ACTIVE).findFirst();
        }

        @Override
        public Optional<FocusSession> findById(long id) {
            return sessions.stream().filter(s -> s.id() == id).findFirst();
        }

        @Override
        public List<FocusSession> findByTaskId(long taskId) {
            return sessions.stream().filter(s -> s.taskId() != null && s.taskId() == taskId).toList();
        }

        @Override
        public boolean update(FocusSession session) {
            for (int i = 0; i < sessions.size(); i++) {
                if (sessions.get(i).id().equals(session.id())) {
                    sessions.set(i, session);
                    return true;
                }
            }
            return false;
        }

        @Override
        public long calculateTotalFocusSecondsForTask(long taskId) {
            return sessions.stream()
                .filter(s -> s.taskId() != null && s.taskId() == taskId && s.status() == TimerStatus.COMPLETED)
                .mapToLong(FocusSession::actualDurationSeconds)
                .sum();
        }
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
            return tasks;
        }

        @Override
        public boolean update(Task task) {
            return true;
        }

        @Override
        public boolean deleteById(long id) {
            return true;
        }
    }
}
