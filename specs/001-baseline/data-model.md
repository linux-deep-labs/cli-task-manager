# Data Model & SQLite Schema Design: CLI Task Manager & Focus Timer

**Branch**: `001-baseline` | **Date**: 2026-08-13 | **Spec**: [spec.md](./spec.md) | **ADR**: [ADR-0001](../../adr/0001-task-persistence-with-sqlite.md)

## Domain Models (Java Records & Value Objects)

### 1. `Task` (Domain Entity)

Represents a work item created and managed by the user.

- **Attributes**:
  - `id`: `Long` (Unique identifier, assigned by SQLite auto-increment)
  - `title`: `String` (Non-null, non-blank title)
  - `description`: `String` (Nullable optional detailed description)
  - `status`: `TaskStatus` (`PENDING`, `IN_PROGRESS`, `COMPLETED`)
  - `priority`: `TaskPriority` (`LOW`, `MEDIUM`, `HIGH`)
  - `dueDate`: `LocalDate` (Nullable ISO date `YYYY-MM-DD`)
  - `createdAt`: `Instant` (Timestamp of creation)
  - `completedAt`: `Instant` (Nullable timestamp of completion)
  - `totalFocusSeconds`: `long` (Calculated or aggregated total focus duration in seconds)

### 2. `FocusSession` (Domain Entity)

Represents a focus session (Pomodoro or timed focus period).

- **Attributes**:
  - `id`: `Long` (Unique identifier)
  - `taskId`: `Long` (Nullable foreign key to `Task`)
  - `plannedDurationMinutes`: `int` (Planned duration, default 25)
  - `startedAt`: `Instant` (Non-null start timestamp)
  - `endedAt`: `Instant` (Nullable end timestamp)
  - `actualDurationSeconds`: `long` (Calculated elapsed duration in seconds)
  - `status`: `TimerStatus` (`ACTIVE`, `COMPLETED`, `CANCELLED`)

### 3. Enumerations

- **`TaskStatus`**: `PENDING`, `IN_PROGRESS`, `COMPLETED`
- **`TaskPriority`**: `LOW`, `MEDIUM`, `HIGH`
- **`TimerStatus`**: `ACTIVE`, `COMPLETED`, `CANCELLED`

---

## SQLite Database Schema (ADR 0001 Compliance)

Database file: `~/.cli-task-manager/tasks.db`

### Table 1: `tasks`

```sql
CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    status TEXT NOT NULL CHECK(status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED')),
    priority TEXT NOT NULL CHECK(priority IN ('LOW', 'MEDIUM', 'HIGH')),
    due_date TEXT,
    created_at TEXT NOT NULL,
    completed_at TEXT
);

CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);
```

### Table 2: `focus_sessions`

```sql
CREATE TABLE IF NOT EXISTS focus_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER REFERENCES tasks(id) ON DELETE SET NULL,
    planned_duration_minutes INTEGER NOT NULL,
    actual_duration_seconds INTEGER NOT NULL DEFAULT 0,
    started_at TEXT NOT NULL,
    ended_at TEXT,
    status TEXT NOT NULL CHECK(status IN ('ACTIVE', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_focus_sessions_task_id ON focus_sessions(task_id);
CREATE INDEX IF NOT EXISTS idx_focus_sessions_status ON focus_sessions(status);
```

---

## Persistence Repositories (Interfaces & Implementations)

### Interface: `TaskRepository`

```java
public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(long id);
    List<Task> findAll(TaskFilter filter);
    boolean update(Task task);
    boolean deleteById(long id);
}
```

### Interface: `FocusSessionRepository`

```java
public interface FocusSessionRepository {
    FocusSession save(FocusSession session);
    Optional<FocusSession> findActiveSession();
    Optional<FocusSession> findById(long id);
    List<FocusSession> findByTaskId(long taskId);
    boolean update(FocusSession session);
    long calculateTotalFocusSecondsForTask(long taskId);
}
```

### Implementation Classes (SQLite JDBC)

- `com.clitaskmanager.persistence.sqlite.SqliteTaskRepository`
- `com.clitaskmanager.persistence.sqlite.SqliteFocusSessionRepository`
- `com.clitaskmanager.persistence.sqlite.DatabaseConnectionManager`
