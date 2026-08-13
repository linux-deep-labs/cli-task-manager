package com.clitaskmanager.domain.model;

import java.time.Instant;

public record FocusSession(
    Long id,
    Long taskId,
    int plannedDurationMinutes,
    long actualDurationSeconds,
    Instant startedAt,
    Instant endedAt,
    TimerStatus status
) {
    public FocusSession {
        if (plannedDurationMinutes <= 0) {
            throw new IllegalArgumentException("Planned duration must be positive");
        }
        if (startedAt == null) {
            startedAt = Instant.now();
        }
        if (status == null) {
            status = TimerStatus.ACTIVE;
        }
    }

    public static FocusSession createNew(Long taskId, int plannedDurationMinutes) {
        return new FocusSession(
            null,
            taskId,
            plannedDurationMinutes,
            0L,
            Instant.now(),
            null,
            TimerStatus.ACTIVE
        );
    }
}
