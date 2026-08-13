package com.clitaskmanager.persistence.api;

import com.clitaskmanager.domain.model.FocusSession;

import java.util.List;
import java.util.Optional;

public interface FocusSessionRepository {
    FocusSession save(FocusSession session);
    Optional<FocusSession> findActiveSession();
    Optional<FocusSession> findById(long id);
    List<FocusSession> findByTaskId(long taskId);
    boolean update(FocusSession session);
    long calculateTotalFocusSecondsForTask(long taskId);
}
