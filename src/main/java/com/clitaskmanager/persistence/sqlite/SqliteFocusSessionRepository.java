package com.clitaskmanager.persistence.sqlite;

import com.clitaskmanager.domain.model.FocusSession;
import com.clitaskmanager.domain.model.TimerStatus;
import com.clitaskmanager.persistence.api.FocusSessionRepository;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteFocusSessionRepository implements FocusSessionRepository {

    @Override
    public FocusSession save(FocusSession session) {
        String sql = """
            INSERT INTO focus_sessions (task_id, planned_duration_minutes, actual_duration_seconds, started_at, ended_at, status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (session.taskId() != null) {
                pstmt.setLong(1, session.taskId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setInt(2, session.plannedDurationMinutes());
            pstmt.setLong(3, session.actualDurationSeconds());
            pstmt.setString(4, session.startedAt().toString());
            pstmt.setString(5, session.endedAt() != null ? session.endedAt().toString() : null);
            pstmt.setString(6, session.status().name());

            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long generatedId = rs.getLong(1);
                    return new FocusSession(
                        generatedId,
                        session.taskId(),
                        session.plannedDurationMinutes(),
                        session.actualDurationSeconds(),
                        session.startedAt(),
                        session.endedAt(),
                        session.status()
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save focus session into SQLite database", e);
        }
        throw new RuntimeException("Failed to retrieve generated ID for focus session");
    }

    @Override
    public Optional<FocusSession> findActiveSession() {
        String sql = "SELECT * FROM focus_sessions WHERE status = 'ACTIVE' ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapResultSetToFocusSession(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query active focus session from SQLite database", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<FocusSession> findById(long id) {
        String sql = "SELECT * FROM focus_sessions WHERE id = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFocusSession(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query focus session by ID from SQLite database", e);
        }
        return Optional.empty();
    }

    @Override
    public List<FocusSession> findByTaskId(long taskId) {
        String sql = "SELECT * FROM focus_sessions WHERE task_id = ? ORDER BY id ASC";
        List<FocusSession> sessions = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, taskId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToFocusSession(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query focus sessions by task ID from SQLite database", e);
        }
        return sessions;
    }

    @Override
    public boolean update(FocusSession session) {
        String sql = """
            UPDATE focus_sessions
            SET actual_duration_seconds = ?, ended_at = ?, status = ?
            WHERE id = ?
            """;
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, session.actualDurationSeconds());
            pstmt.setString(2, session.endedAt() != null ? session.endedAt().toString() : null);
            pstmt.setString(3, session.status().name());
            pstmt.setLong(4, session.id());

            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update focus session in SQLite database", e);
        }
    }

    @Override
    public long calculateTotalFocusSecondsForTask(long taskId) {
        String sql = "SELECT COALESCE(SUM(actual_duration_seconds), 0) FROM focus_sessions WHERE task_id = ? AND status = 'COMPLETED'";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, taskId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate total focus time for task from SQLite database", e);
        }
        return 0L;
    }

    private FocusSession mapResultSetToFocusSession(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long taskIdVal = rs.getLong("task_id");
        Long taskId = rs.wasNull() ? null : taskIdVal;
        int plannedMinutes = rs.getInt("planned_duration_minutes");
        long actualSeconds = rs.getLong("actual_duration_seconds");
        Instant startedAt = Instant.parse(rs.getString("started_at"));
        
        String endedAtStr = rs.getString("ended_at");
        Instant endedAt = endedAtStr != null ? Instant.parse(endedAtStr) : null;
        
        TimerStatus status = TimerStatus.valueOf(rs.getString("status"));

        return new FocusSession(id, taskId, plannedMinutes, actualSeconds, startedAt, endedAt, status);
    }
}
