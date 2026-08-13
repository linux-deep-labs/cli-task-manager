package com.clitaskmanager.persistence.sqlite;

import com.clitaskmanager.domain.model.Task;
import com.clitaskmanager.domain.model.TaskPriority;
import com.clitaskmanager.domain.model.TaskStatus;
import com.clitaskmanager.persistence.api.TaskRepository;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTaskRepository implements TaskRepository {

    @Override
    public Task save(Task task) {
        String sql = """
            INSERT INTO tasks (title, description, status, priority, due_date, created_at, completed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, task.title());
            pstmt.setString(2, task.description());
            pstmt.setString(3, task.status().name());
            pstmt.setString(4, task.priority().name());
            pstmt.setString(5, task.dueDate() != null ? task.dueDate().toString() : null);
            pstmt.setString(6, task.createdAt() != null ? task.createdAt().toString() : Instant.now().toString());
            pstmt.setString(7, task.completedAt() != null ? task.completedAt().toString() : null);

            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long generatedId = rs.getLong(1);
                    return new Task(
                        generatedId,
                        task.title(),
                        task.description(),
                        task.status(),
                        task.priority(),
                        task.dueDate(),
                        task.createdAt(),
                        task.completedAt(),
                        0L
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save task into SQLite database", e);
        }
        throw new RuntimeException("Failed to retrieve generated ID for task");
    }

    @Override
    public Optional<Task> findById(long id) {
        String sql = """
            SELECT t.id, t.title, t.description, t.status, t.priority, t.due_date, t.created_at, t.completed_at,
                   COALESCE(SUM(fs.actual_duration_seconds), 0) AS total_focus
            FROM tasks t
            LEFT JOIN focus_sessions fs ON t.id = fs.task_id AND fs.status = 'COMPLETED'
            WHERE t.id = ?
            GROUP BY t.id
            """;
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query task by ID from SQLite database", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Task> findAll(TaskStatus statusFilter, TaskPriority priorityFilter) {
        StringBuilder sql = new StringBuilder("""
            SELECT t.id, t.title, t.description, t.status, t.priority, t.due_date, t.created_at, t.completed_at,
                   COALESCE(SUM(fs.actual_duration_seconds), 0) AS total_focus
            FROM tasks t
            LEFT JOIN focus_sessions fs ON t.id = fs.task_id AND fs.status = 'COMPLETED'
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();
        if (statusFilter != null) {
            sql.append(" AND t.status = ?");
            params.add(statusFilter.name());
        }
        if (priorityFilter != null) {
            sql.append(" AND t.priority = ?");
            params.add(priorityFilter.name());
        }
        sql.append(" GROUP BY t.id ORDER BY t.id ASC");

        List<Task> tasks = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list tasks from SQLite database", e);
        }
        return tasks;
    }

    @Override
    public boolean update(Task task) {
        String sql = """
            UPDATE tasks
            SET title = ?, description = ?, status = ?, priority = ?, due_date = ?, completed_at = ?
            WHERE id = ?
            """;
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.title());
            pstmt.setString(2, task.description());
            pstmt.setString(3, task.status().name());
            pstmt.setString(4, task.priority().name());
            pstmt.setString(5, task.dueDate() != null ? task.dueDate().toString() : null);
            pstmt.setString(6, task.completedAt() != null ? task.completedAt().toString() : null);
            pstmt.setLong(7, task.id());

            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update task in SQLite database", e);
        }
    }

    @Override
    public boolean deleteById(long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            int deleted = pstmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete task from SQLite database", e);
        }
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        TaskStatus status = TaskStatus.valueOf(rs.getString("status"));
        TaskPriority priority = TaskPriority.valueOf(rs.getString("priority"));
        
        String dueDateStr = rs.getString("due_date");
        LocalDate dueDate = dueDateStr != null ? LocalDate.parse(dueDateStr) : null;
        
        String createdAtStr = rs.getString("created_at");
        Instant createdAt = createdAtStr != null ? Instant.parse(createdAtStr) : null;
        
        String completedAtStr = rs.getString("completed_at");
        Instant completedAt = completedAtStr != null ? Instant.parse(completedAtStr) : null;
        
        long totalFocus = rs.getLong("total_focus");

        return new Task(id, title, description, status, priority, dueDate, createdAt, completedAt, totalFocus);
    }
}
