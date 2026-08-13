# Technical Implementation Plan: CLI Task Manager & Focus Timer

**Branch**: `001-baseline` | **Date**: 2026-08-13 | **Spec**: [spec.md](./spec.md) | **ADR**: [ADR-0001](../../adr/0001-task-persistence-with-sqlite.md)

---

## Summary

This plan outlines the technical design for a greenfield developer productivity CLI application (**CLI Task Manager & Focus Timer**) built in **Java 21** with **Gradle**, strictly adhering to **ADR 0001** and the project **Constitution**. The application provides complete task lifecycle management (create, list, update, complete, delete) and an integrated 25-minute focus timer (start, stop, status), using **SQLite** (`org.xerial:sqlite-jdbc`) as the exclusive persistent storage strategy.

---

## Technical Context

- **Language/Version**: Java 21 LTS
- **Build Tool**: Gradle (Java Application Plugin)
- **Primary Dependencies**:
  - `org.xerial:sqlite-jdbc` (SQLite JDBC driver)
  - `info.picocli:picocli` (CLI command parser & terminal output formatter)
  - `org.slf4j:slf4j-simple` (Lightweight logging provider)
- **Storage Engine**: SQLite (`~/.cli-task-manager/tasks.db`)
- **Testing Framework**: JUnit 5 (`org.junit.jupiter`), AssertJ (`org.assertj:assertj-core`)
- **Target Platform**: POSIX-compliant CLI (Linux, macOS) and Windows terminal environments
- **Project Type**: Standalone CLI Application
- **Performance Goals**: Sub-100ms response targets for all local CLI commands
- **Constraints**:
  - Task persistence MUST use SQLite.
  - Local JSON files MUST NOT be used for task persistence.
  - In-memory-only persistence MUST NOT be used for task persistence.
  - SQLite must remain the single persistence strategy unless ADR 0001 is formally superseded.

---

## Constitution & ADR 0001 Compliance Check

*GATE: Passed before Phase 0 research and verified post Phase 1 design.*

| Governance / Architectural Requirement | Compliance Status | Implementation Strategy |
|---------------------------------------|-------------------|-------------------------|
| **Principle I & ADR 0001**: SQLite Task Persistence | **PASSED** | Persistence is provided exclusively via `SqliteTaskRepository` and `SqliteFocusSessionRepository` using JDBC. |
| **Constraint 4**: No JSON File Storage | **PASSED** | No flat-file or JSON repositories exist in the architecture. |
| **Constraint 4**: No In-Memory Persistence | **PASSED** | In-memory storage is prohibited for application data persistence. |
| **Principle II**: Separation of Concerns | **PASSED** | Clear 3-tier layering: `cli` -> `service` -> `persistence` repository interface. |
| **Principle III**: Automated Test Coverage | **PASSED** | Unit tests for domain services + SQLite integration tests with JUnit 5. |
| **Principle V**: Verifiable Implementation | **PASSED** | `ArchitecturalComplianceTest` enforces SQLite database connection and repository abstractions. |

---

## 1. Project Structure

Standard Gradle Java Application project layout:

```text
cli-task-manager/
├── build.gradle
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── clitaskmanager/
│   │   │           ├── Main.java                        # CLI Main Entry Point
│   │   │           ├── cli/                             # CLI Commands & Presentation Layer
│   │   │           │   ├── TaskCommand.java             # `task` subcommand router
│   │   │           │   ├── TaskAddCommand.java          # `task add`
│   │   │           │   ├── TaskListCommand.java         # `task list`
│   │   │           │   ├── TaskUpdateCommand.java       # `task update`
│   │   │           │   ├── TaskCompleteCommand.java     # `task complete`
│   │   │           │   ├── TaskDeleteCommand.java       # `task delete`
│   │   │           │   ├── TaskInfoCommand.java         # `task info`
│   │   │           │   ├── TimerCommand.java            # `timer` subcommand router
│   │   │           │   ├── TimerStartCommand.java       # `timer start`
│   │   │           │   ├── TimerStopCommand.java        # `timer stop`
│   │   │           │   └── TimerStatusCommand.java      # `timer status`
│   │   │           ├── domain/                          # Business Logic & Domain Models
│   │   │           │   ├── model/
│   │   │           │   │   ├── Task.java                # Task Record/Entity
│   │   │           │   │   ├── TaskStatus.java          # Enum (PENDING, IN_PROGRESS, COMPLETED)
│   │   │           │   │   ├── TaskPriority.java        # Enum (LOW, MEDIUM, HIGH)
│   │   │           │   │   ├── FocusSession.java        # FocusSession Record/Entity
│   │   │           │   │   └── TimerStatus.java         # Enum (ACTIVE, COMPLETED, CANCELLED)
│   │   │           │   └── service/
│   │   │           │       ├── TaskService.java         # Task domain workflows
│   │   │           │       └── FocusTimerService.java   # Focus timer domain workflows
│   │   │           └── persistence/                     # SQLite Storage Layer (ADR 0001)
│   │   │               ├── api/
│   │   │               │   ├── TaskRepository.java      # Task Repository Interface
│   │   │               │   └── FocusSessionRepository.java # FocusSession Repository Interface
│   │   │               └── sqlite/
│   │   │                   ├── DatabaseConnectionManager.java # SQLite JDBC Connection Provider
│   │   │                   ├── SqliteTaskRepository.java      # SQLite Task Implementation
│   │   │                   └── SqliteFocusSessionRepository.java # SQLite Focus Implementation
│   │   └── resources/
│   │       └── db/
│   │           └── migration/
│   │               └── V1__initial_schema.sql           # Initial SQLite DDL Script
│   └── test/
│       └── java/
│           └── com/
│               └── clitaskmanager/
│                   ├── domain/                          # Unit Tests for Domain Services
│                   │   ├── TaskServiceTest.java
│                   │   └── FocusTimerServiceTest.java
│                   ├── persistence/                     # SQLite Integration Tests
│                   │   ├── SqliteTaskRepositoryTest.java
│                   │   └── SqliteFocusSessionRepositoryTest.java
│                   └── compliance/                      # Architectural Compliance Verification
│                       └── ArchitecturalComplianceTest.java
```

---

## 2. Technology Choices

1. **Java 21 LTS**: Offers strong typing, immutable `record` components for domain objects, and modern concurrency.
2. **Gradle**: High-performance Java build automation, dependency resolution, and test runner execution.
3. **SQLite JDBC (`org.xerial:sqlite-jdbc`)**: Embedded relational database requiring zero external service setup. Delivers transaction safety and single-file portability (`tasks.db`).
4. **Picocli (`info.picocli:picocli`)**: Declarative CLI parsing with automatic subcommand dispatch, option parsing, and terminal color/formatting support.
5. **JUnit 5 & AssertJ**: Standard Java testing tools for isolated unit tests and SQLite integration tests.

---

## 3. Domain Model Design

The domain logic relies on immutable Java `record` entities decoupled from persistence implementation details:

- `Task`: Holds ID, title, description, status (`PENDING`, `IN_PROGRESS`, `COMPLETED`), priority (`LOW`, `MEDIUM`, `HIGH`), optional due date, created/completed timestamps, and total focus time.
- `FocusSession`: Holds ID, linked `taskId`, planned duration (minutes), actual focused seconds, start/end timestamps, and status (`ACTIVE`, `COMPLETED`, `CANCELLED`).
- `TaskService`: Business rules for creating, filtering, updating, completing, and deleting tasks.
- `FocusTimerService`: Business rules for starting, checking, and stopping focus sessions, enforcing the rule that only ONE active timer can run at a time.

---

## 4. SQLite Persistence Design (ADR 0001 Enforcement)

### Database Connection & Initialization
- `DatabaseConnectionManager` connects via `jdbc:sqlite:~/.cli-task-manager/tasks.db`.
- On initial startup, executes `V1__initial_schema.sql` to initialize SQLite tables if they do not exist:

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

CREATE TABLE IF NOT EXISTS focus_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER REFERENCES tasks(id) ON DELETE SET NULL,
    planned_duration_minutes INTEGER NOT NULL,
    actual_duration_seconds INTEGER NOT NULL DEFAULT 0,
    started_at TEXT NOT NULL,
    ended_at TEXT,
    status TEXT NOT NULL CHECK(status IN ('ACTIVE', 'COMPLETED', 'CANCELLED'))
);
```

### Abstraction & Isolation
- Domain services consume `TaskRepository` and `FocusSessionRepository` Java interfaces.
- JDBC queries and `PreparedStatement` mappings are strictly encapsulated inside `SqliteTaskRepository` and `SqliteFocusSessionRepository`.

---

## 5. CLI Interaction Design

Commands operate via standard subcommand hierarchy:

- `task add "<title>" [--description "<desc>"] [--priority <LOW|MEDIUM|HIGH>] [--due <YYYY-MM-DD>]`
- `task list [--status <PENDING|IN_PROGRESS|COMPLETED>] [--priority <LOW|MEDIUM|HIGH>]`
- `task update <id> [--title "<title>"] [--description "<desc>"] [--priority <p>] [--due <YYYY-MM-DD>]`
- `task complete <id>`
- `task delete <id>`
- `task info <id>`
- `timer start [--task-id <id>] [--duration <minutes>]`
- `timer status`
- `timer stop`

---

## 6. Task Management Flow

1. User invokes `task add "Title"`. `TaskAddCommand` validates arguments and delegates to `TaskService.createTask(...)`.
2. `TaskService` instantiates a `Task` entity with status `PENDING` and calls `TaskRepository.save(task)`.
3. `SqliteTaskRepository` executes SQL `INSERT INTO tasks ...` and returns saved `Task` with auto-generated ID.
4. User invokes `task complete <id>`. `TaskService` updates task status to `COMPLETED`, records `completedAt = Instant.now()`, and calls `TaskRepository.update(task)`.

---

## 7. 25-Minute Focus Timer Implementation

1. **Starting a Timer**:
   - `timer start --task-id 1 --duration 25` (default 25 minutes).
   - `FocusTimerService` queries `FocusSessionRepository.findActiveSession()`.
   - If an active session exists, it rejects the command with an error message ("Timer already running").
   - If no active session exists, it creates a `FocusSession` with `status = ACTIVE`, `startedAt = Instant.now()`, and saves it to SQLite.
2. **Checking Status**:
   - `timer status` queries `FocusSessionRepository.findActiveSession()`.
   - If found, computes `elapsedSeconds = Duration.between(startedAt, Instant.now()).getSeconds()` and displays remaining time.
3. **Stopping Timer**:
   - `timer stop` fetches the active session, calculates final `actualDurationSeconds`, sets `status = COMPLETED` (or `CANCELLED`), records `endedAt = Instant.now()`, and updates SQLite.

---

## 8. Error Handling & Standard Exit Codes

- **Input Validation Errors**: Output message to `stderr`, exit with code `1`.
- **Resource Not Found**: Output task/timer not found error to `stderr`, exit with code `2`.
- **Database/SQLite Access Failures**: Log detailed message to `stderr`, exit with code `3`.

---

## 9. Testing Strategy

1. **Unit Tests (`com.clitaskmanager.domain.*`)**:
   - Fast, isolated unit tests using JUnit 5 and mock repositories to test `TaskService` state transitions and `FocusTimerService` rules.
2. **SQLite Integration Tests (`com.clitaskmanager.persistence.sqlite.*`)**:
   - Integration tests executing real SQL statements against temporary SQLite database instances to verify CRUD operations, transactions, and foreign key cascades.
3. **CLI End-to-End Tests**:
   - Executing command-line invocations via Picocli test harness and validating `stdout`, `stderr`, and exit codes.

---

## 10. How ADR 0001 Will Be Enforced

- **Repository Isolation**: The domain layer only accepts `TaskRepository` interfaces.
- **Forbidden Technologies**: No Jackson, Gson, or flat-file IO libraries are imported for storage purposes.
- **CI Build Check**: Automated test suites fail if any persistence implementation other than SQLite is detected.

---

## 11. Architectural Compliance Verification

Compliance with ADR 0001 and Constitution Principle I will be verified via `ArchitecturalComplianceTest`:

```java
@Test
void verifySqliteIsSolePersistenceProvider() {
    // 1. Verify DatabaseConnectionManager connects via org.sqlite.JDBC driver
    assertTrue(DatabaseConnectionManager.getDriverName().contains("SQLite"));
    
    // 2. Verify SqliteTaskRepository is the active TaskRepository implementation
    assertTrue(taskRepository instanceof SqliteTaskRepository);
    
    // 3. Verify SQLite database file is created at target path
    assertTrue(DatabaseConnectionManager.getDatabaseFile().exists());
}
```

---

## Artifacts Generated

- `specs/001-baseline/plan.md` (This document)
- `specs/001-baseline/research.md` (Phase 0 technical research)
- `specs/001-baseline/data-model.md` (Phase 1 entity models & SQLite schema)
- `specs/001-baseline/contracts/cli-contract.md` (Phase 1 CLI command syntax contract)
- `specs/001-baseline/quickstart.md` (Phase 1 runnable validation guide)
