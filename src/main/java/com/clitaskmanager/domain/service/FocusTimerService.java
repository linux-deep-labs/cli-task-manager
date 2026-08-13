package com.clitaskmanager.domain.service;

import com.clitaskmanager.domain.model.FocusSession;
import com.clitaskmanager.domain.model.TimerStatus;
import com.clitaskmanager.persistence.api.FocusSessionRepository;
import com.clitaskmanager.persistence.api.TaskRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class FocusTimerService {

    private final FocusSessionRepository focusSessionRepository;
    private final TaskRepository taskRepository;

    public FocusTimerService(FocusSessionRepository focusSessionRepository, TaskRepository taskRepository) {
        this.focusSessionRepository = focusSessionRepository;
        this.taskRepository = taskRepository;
    }

    public FocusSession startTimer(Long taskId, int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Timer duration must be greater than 0 minutes");
        }
        if (taskId != null) {
            taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task #" + taskId + " not found"));
        }

        Optional<FocusSession> active = focusSessionRepository.findActiveSession();
        if (active.isPresent()) {
            throw new IllegalStateException("Focus timer is already running (Session #" + active.get().id() + ")");
        }

        FocusSession session = FocusSession.createNew(taskId, durationMinutes);
        return focusSessionRepository.save(session);
    }

    public Optional<FocusSession> getActiveTimer() {
        return focusSessionRepository.findActiveSession();
    }

    public FocusSession stopTimer() {
        FocusSession active = focusSessionRepository.findActiveSession()
            .orElseThrow(() -> new IllegalStateException("No active focus timer session is currently running"));

        Instant now = Instant.now();
        long actualSeconds = Duration.between(active.startedAt(), now).getSeconds();

        FocusSession stopped = new FocusSession(
            active.id(),
            active.taskId(),
            active.plannedDurationMinutes(),
            actualSeconds,
            active.startedAt(),
            now,
            TimerStatus.COMPLETED
        );

        focusSessionRepository.update(stopped);
        return stopped;
    }
}
