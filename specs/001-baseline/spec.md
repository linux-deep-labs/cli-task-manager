# Feature Specification: Baseline Specification - CLI Task Manager & Focus Timer

**Feature Branch**: `001-baseline`

**Created**: 2026-08-13

**Status**: Approved Baseline

**Input**: User description: "Baseline specification for greenfield developer productivity CLI application with task management (create, list, update, complete, delete, metadata, states) and focus timer (start, stop, record sessions, view task & focus stats) following ADR-0001 SQLite persistence."

## Overview & User-Facing CLI Behavior

The **CLI Task Manager & Focus Timer** is a lightweight developer productivity command-line utility. It empowers developers to manage daily work items, track task lifecycles, run focused Pomodoro/timer sessions tied to specific tasks, and view productivity metrics directly in their terminal.

### Command Line Interface Summary

- `task add "<title>" [--description "<desc>"] [--priority <p>] [--due <yyyy-mm-dd>]`
  Creates a new task with initial status `pending`.
- `task list [--status <pending|in_progress|completed>] [--priority <p>]`
  Lists tasks in readable tabular or formatted views, displaying ID, title, status, priority, due date, and total focus time.
- `task update <id> [--title "<title>"] [--description "<desc>"] [--priority <p>] [--due <yyyy-mm-dd>]`
  Updates metadata for an existing task.
- `task complete <id>`
  Marks a task as `completed` with a completion timestamp.
- `task delete <id>`
  Permanently removes a task and its historical data.
- `timer start [--task-id <id>] [--duration <minutes>]`
  Starts an active focus session (defaulting to 25 minutes if unspecified) optionally associated with a task.
- `timer stop`
  Stops the active focus session and logs recorded focus time.
- `timer status`
  Displays the current timer state (running/idle, remaining time, active task).
- `task info <id>`
  Views full details for a task including metadata, status history, and total logged focus sessions.

---

## User Scenarios & Testing

### User Story 1 - Task Lifecycle Management (Priority: P1)

As a developer, I want to create, view, update, complete, and delete tasks via CLI commands so that I can maintain an organized list of work directly in my command-line terminal environment.

**Why this priority**: Core task CRUD operations form the foundational value of a task manager. Without task creation and tracking, timer features cannot link to tasks.

**Independent Test**: Can be tested independently by running `task add`, `task list`, `task update`, `task complete`, and `task delete` commands, verifying state updates in persistent storage.

**Acceptance Scenarios**:

1. **Given** no existing tasks, **When** I run `task add "Fix bug #123" --priority High`, **Then** a new task is created with status `pending`, unique numeric ID, and priority `High`, and confirmed in CLI output.
2. **Given** existing tasks, **When** I run `task list`, **Then** all active tasks are listed with their ID, title, priority, due date, and status.
3. **Given** a pending task with ID `1`, **When** I run `task complete 1`, **Then** the task status transitions to `completed`, records a completion timestamp, and displays success feedback.
4. **Given** an existing task with ID `1`, **When** I run `task delete 1`, **Then** the task is removed and no longer appears in `task list`.

---

### User Story 2 - Integrated Focus Timer & Session Logging (Priority: P2)

As a developer, I want to start and stop focus sessions tied to specific tasks so that I can track dedicated focus time spent on individual items.

**Why this priority**: Combining focus timing with task management enables developer productivity tracking and flow preservation.

**Independent Test**: Can be tested independently by executing `timer start --task-id 1 --duration 25`, querying `timer status`, running `timer stop`, and verifying that the focus session is recorded and aggregated under task `#1`.

**Acceptance Scenarios**:

1. **Given** an active task `#1`, **When** I run `timer start --task-id 1 --duration 25`, **Then** a 25-minute focus session starts, associated with task `#1`, and timer status reports "Running".
2. **Given** a running timer session, **When** I run `timer status`, **Then** the CLI displays the elapsed time, remaining duration, and linked task title.
3. **Given** a running timer session, **When** I run `timer stop`, **Then** the timer stops, logs the elapsed focus time to SQLite, and updates total focus duration on task `#1`.
4. **Given** no running timer session, **When** I run `timer stop`, **Then** the system displays a clear error indicating no active session is running.

---

### User Story 3 - Comprehensive Task & Focus Insights (Priority: P3)

As a developer, I want to view detailed task metadata alongside focus stats so that I can evaluate time allocation and task progress.

**Why this priority**: Provides actionable insights into developer productivity and time management.

**Independent Test**: Can be tested independently by executing `task info 1` on a task with recorded focus sessions and checking formatted summary output.

**Acceptance Scenarios**:

1. **Given** a task `#1` with 2 completed focus sessions totaling 50 minutes, **When** I run `task info 1`, **Then** the system displays complete metadata, status, created date, completion date, and list of focus sessions.

---

### Edge Cases

- **Concurrent Timer Prevention**: Attempting to run `timer start` while a session is already active MUST produce an error asking the user to stop or cancel the existing session first.
- **Deleted Task Handling**: Deleting a task that has active or historic focus sessions MUST properly clean up or cascade-delete dependent records without corrupting SQLite database state.
- **Corrupted Database Recovery**: If SQLite database file is unreadable or locked, the application MUST present a clear error to `stderr` with non-zero exit code without crashing silently.
- **Zero or Negative Timer Duration**: Passing `--duration 0` or negative values MUST be rejected with command validation feedback.

---

## Requirements

### Functional Requirements

#### Task Management & Lifecycle
- **FR-001**: System MUST support creating tasks with `title` (required), `description` (optional), `priority` (`Low`, `Medium`, `High`; default `Medium`), and `due_date` (optional, ISO format `YYYY-MM-DD`).
- **FR-002**: System MUST assign a unique incrementing integer ID to each created task.
- **FR-003**: System MUST support task lifecycle states: `pending` -> `in_progress` -> `completed`.
- **FR-004**: System MUST allow filtering task listings by status (`pending`, `in_progress`, `completed`) and priority.
- **FR-005**: System MUST allow updating task fields (`title`, `description`, `priority`, `due_date`).
- **FR-006**: System MUST support completing tasks, setting status to `completed` and logging `completed_at` timestamp.
- **FR-007**: System MUST support deleting tasks permanently by ID.

#### Focus Timer Management
- **FR-008**: System MUST allow starting a focus timer session with configurable duration (in minutes, default 25).
- **FR-009**: System MUST allow linking a focus session to an existing task ID or running an unlinked standalone session.
- **FR-010**: System MUST enforce that only ONE focus timer session can be active at any given time.
- **FR-011**: System MUST allow checking the real-time status of the active timer (`running`/`idle`, elapsed, remaining, linked task).
- **FR-012**: System MUST allow stopping an active focus timer session early or letting it complete, recording `started_at`, `ended_at`, `duration_minutes`, and `actual_seconds_focused`.
- **FR-013**: System MUST calculate and store total accumulated focus time per task.

#### Persistence Requirements (ADR-0001 Alignment)
- **FR-014**: System MUST use SQLite as the authoritative persistent data store for all tasks, metadata, and focus timer session logs per ADR-0001.
- **FR-015**: System MUST NOT use JSON files, flat files, or in-memory-only structures as primary or substitute persistent storage.
- **FR-016**: System MUST enforce clean separation between domain/business logic and SQLite database persistence operations using data repository interfaces.

#### Error Handling & CLI Output
- **FR-017**: System MUST output human-readable standard output (`stdout`) for successful operations and diagnostic error messages (`stderr`) with standard exit codes (`0` for success, non-zero for error).
- **FR-018**: System MUST validate input parameters (non-empty titles, valid dates, positive durations, valid task IDs) and return clear user guidance on validation failures.

### Key Entities

- **Task Entity**:
  - `id`: Integer (Primary Key, Auto-increment)
  - `title`: String (Required)
  - `description`: Text (Optional)
  - `status`: Enum (`pending`, `in_progress`, `completed`)
  - `priority`: Enum (`Low`, `Medium`, `High`)
  - `due_date`: Date/String (Optional, `YYYY-MM-DD`)
  - `created_at`: Timestamp (Auto-generated)
  - `completed_at`: Timestamp (Nullable)
- **FocusSession Entity**:
  - `id`: Integer (Primary Key, Auto-increment)
  - `task_id`: Integer (Foreign Key to Task, Nullable for unlinked sessions)
  - `planned_duration_minutes`: Integer (Required)
  - `actual_duration_seconds`: Integer (Required)
  - `started_at`: Timestamp (Required)
  - `ended_at`: Timestamp (Nullable)
  - `status`: Enum (`active`, `completed`, `cancelled`)

---

## Success Criteria

### Measurable Outcomes

- **SC-001**: All CLI commands (`task add`, `list`, `update`, `complete`, `delete`, `timer start`, `stop`, `status`) execute and return output in under 100 milliseconds for standard local databases.
- **SC-002**: 100% of task data and focus timer logs are persistently saved to SQLite across process exits and system restarts.
- **SC-003**: Zero data corruption or loss occurs during unexpected command interrupts (`SIGINT` / `Ctrl+C`).
- **SC-004**: 100% of core task behavior and focus timer logic are covered by automated unit and integration test suites.

---

## Assumptions

- **Local Execution**: The application runs as a local CLI binary on POSIX-compliant systems (Linux, macOS) or Windows terminals.
- **Single-User Workspace**: The local SQLite database file defaults to a standard user application data path (e.g., `~/.cli-task-manager/tasks.db` or current directory config).
- **Time Accuracy**: The host system clock is trusted for timestamp recording.
