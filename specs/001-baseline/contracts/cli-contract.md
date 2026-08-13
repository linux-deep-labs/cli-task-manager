# CLI Command Interface Contract: CLI Task Manager & Focus Timer

**Branch**: `001-baseline` | **Date**: 2026-08-13 | **Spec**: [spec.md](../spec.md)

## Command Line Interface Schema & Syntax

Executable entry point: `task` (or `java -jar cli-task-manager.jar`)

---

## 1. Task Management Commands

### 1.1 `task add`
Creates a new task with initial status `PENDING`.

- **Syntax**: `task add "<title>" [--description "<desc>"] [--priority <LOW|MEDIUM|HIGH>] [--due <YYYY-MM-DD>]`
- **Output (`stdout`)**:
  ```text
  Task #1 created successfully.
  Title: Fix bug #123
  Priority: HIGH
  Status: PENDING
  ```
- **Errors (`stderr`)**:
  - Exit code `1`: Empty title or invalid date format `YYYY-MM-DD`.

### 1.2 `task list`
Lists tasks with optional status/priority filters.

- **Syntax**: `task list [--status <PENDING|IN_PROGRESS|COMPLETED>] [--priority <LOW|MEDIUM|HIGH>]`
- **Output (`stdout`)**:
  ```text
  ID  STATUS       PRIORITY  DUE DATE    FOCUS TIME  TITLE
  ----------------------------------------------------------------
  1   PENDING      HIGH      2026-08-20  00:50:00    Fix bug #123
  2   IN_PROGRESS  MEDIUM    -           00:25:00    Write documentation
  ```

### 1.3 `task update`
Updates metadata for an existing task.

- **Syntax**: `task update <id> [--title "<title>"] [--description "<desc>"] [--priority <p>] [--due <YYYY-MM-DD>]`
- **Output (`stdout`)**:
  ```text
  Task #1 updated successfully.
  ```

### 1.4 `task complete`
Marks a task as completed.

- **Syntax**: `task complete <id>`
- **Output (`stdout`)**:
  ```text
  Task #1 marked as COMPLETED.
  Completed at: 2026-08-13 15:30:00
  ```

### 1.5 `task delete`
Deletes a task permanently.

- **Syntax**: `task delete <id>`
- **Output (`stdout`)**:
  ```text
  Task #1 deleted successfully.
  ```

### 1.6 `task info`
Displays detailed information and focus logs for a task.

- **Syntax**: `task info <id>`
- **Output (`stdout`)**:
  ```text
  Task #1 Details
  ----------------------------------------
  Title: Fix bug #123
  Description: Resolve memory leak in session handler
  Status: COMPLETED
  Priority: HIGH
  Due Date: 2026-08-20
  Created At: 2026-08-13 10:00:00
  Completed At: 2026-08-13 15:30:00
  Total Focus Time: 50m 00s (2 sessions)
  ```

---

## 2. Focus Timer Commands

### 2.1 `timer start`
Starts a focus timer session.

- **Syntax**: `timer start [--task-id <id>] [--duration <minutes>]`
- **Default Duration**: 25 minutes (Pomodoro standard).
- **Output (`stdout`)**:
  ```text
  Focus timer started!
  Duration: 25 minutes
  Linked Task: #1 (Fix bug #123)
  End Time Target: 15:55:00
  ```
- **Errors (`stderr`)**:
  - Exit code `1`: Timer already active. Attempting to start concurrent timers is blocked.

### 2.2 `timer status`
Queries the real-time state of the active timer.

- **Syntax**: `timer status`
- **Output (`stdout`)**:
  ```text
  Timer Status: RUNNING
  Linked Task: #1 (Fix bug #123)
  Elapsed: 12m 30s / 25m 00s
  Remaining: 12m 30s
  ```

### 2.3 `timer stop`
Stops the running focus session and records duration in SQLite.

- **Syntax**: `timer stop`
- **Output (`stdout`)**:
  ```text
  Focus session stopped and saved to database.
  Actual Focus Duration: 15 minutes 45 seconds.
  Recorded under Task #1.
  ```

---

## Standard Exit Codes

- `0`: Operation completed successfully.
- `1`: User input validation error or invalid arguments.
- `2`: Resource not found (e.g., Task ID does not exist).
- `3`: Database access / SQLite I/O failure.
