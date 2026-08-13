# CLI Task Manager & Focus Timer

A lightweight command-line productivity application for managing tasks and tracking focused work sessions.

The project is developed using a **Spec-Driven Development** approach, with explicit architectural decisions, technical planning, automated testing, and final architecture verification.

## Features

### Task Management

- Create tasks
- List tasks
- Update task information
- Complete tasks
- Delete tasks
- View task details
- Set task priority
- Set task description
- Set due dates
- Track task lifecycle

### Focus Timer

- Start a focus session for a task
- 25-minute focus timer
- Check active timer status
- Stop a focus session
- Record completed focus sessions
- Track total focus time
- Prevent multiple active focus sessions

### Persistence

- SQLite is used as the persistent storage mechanism.
- Task and focus session data persist across CLI invocations.
- JSON files are not used for task persistence.
- In-memory-only persistence is not used.

## Architecture

The application follows a simple layered architecture:

```text
CLI
 │
 ▼
Application / Business Logic
 │
 ▼
Repository Interfaces
 │
 ▼
SQLite Repositories
 │
 ▼
SQLite Database