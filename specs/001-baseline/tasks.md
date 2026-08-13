# Actionable Tasks: CLI Task Manager & Focus Timer

**Feature Branch**: `001-baseline` | **Date**: 2026-08-13 | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **ADR**: [ADR-0001](../../adr/0001-task-persistence-with-sqlite.md)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Initialize Java 21 Gradle project with SQLite JDBC (`org.xerial:sqlite-jdbc`), Picocli (`info.picocli:picocli`), and JUnit 5 dependencies in `build.gradle`
- [X] T002 [P] Configure Gradle application main class entry point and package structure under `src/main/java/com/clitaskmanager/`
- [X] T003 [P] Create CLI Main entry point class in `src/main/java/com/clitaskmanager/Main.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain records, SQLite connection manager, DDL schema, and repository implementations that MUST be complete before user stories

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 Create domain model records and enums (`Task`, `TaskStatus`, `TaskPriority`, `FocusSession`, `TimerStatus`) in `src/main/java/com/clitaskmanager/domain/model/`
- [X] T005 [P] Create repository interface abstractions (`TaskRepository`, `FocusSessionRepository`) in `src/main/java/com/clitaskmanager/persistence/api/`
- [X] T006 Implement SQLite database connection manager and schema DDL in `src/main/resources/db/migration/V1__initial_schema.sql` and `src/main/java/com/clitaskmanager/persistence/sqlite/DatabaseConnectionManager.java`
- [X] T007 Implement SQLite task repository (`SqliteTaskRepository`) in `src/main/java/com/clitaskmanager/persistence/sqlite/SqliteTaskRepository.java`
- [X] T008 [P] Implement SQLite focus session repository (`SqliteFocusSessionRepository`) in `src/main/java/com/clitaskmanager/persistence/sqlite/SqliteFocusSessionRepository.java`
- [X] T009 Write SQLite persistence integration test verifying task and focus session CRUD operations in `src/test/java/com/clitaskmanager/persistence/SqliteRepositoryIntegrationTest.java`
- [X] T010 Create architectural fitness function test (`ArchitecturalComplianceTest`) verifying task persistence exclusively uses SQLite JDBC per ADR 0001 in `src/test/java/com/clitaskmanager/compliance/ArchitecturalComplianceTest.java`

**Checkpoint**: Foundation ready - SQLite storage and architectural compliance tests verified. User story implementation can now begin.

---

## Phase 3: User Story 1 - Task Lifecycle Management (Priority: P1) 🎯 MVP

**Goal**: Enable users to create (`add`), view (`list`), update, complete, and delete tasks via CLI commands.

**Independent Test**: Execute `task add`, `task list`, `task update`, `task complete`, and `task delete`, verifying task persistence and state transitions in SQLite.

### Tests for User Story 1
- [X] T011 [P] [US1] Write unit test for `TaskService` lifecycle operations in `src/test/java/com/clitaskmanager/domain/TaskServiceTest.java`

### Implementation for User Story 1
- [X] T012 [US1] Implement `TaskService` for business logic and state transitions in `src/main/java/com/clitaskmanager/domain/service/TaskService.java`
- [X] T013 [P] [US1] Implement `task add` command handler with input validation in `src/main/java/com/clitaskmanager/cli/TaskAddCommand.java`
- [X] T014 [P] [US1] Implement `task list` command handler with status and priority filtering in `src/main/java/com/clitaskmanager/cli/TaskListCommand.java`
- [X] T015 [P] [US1] Implement `task update` command handler in `src/main/java/com/clitaskmanager/cli/TaskUpdateCommand.java`
- [X] T016 [P] [US1] Implement `task complete` command handler in `src/main/java/com/clitaskmanager/cli/TaskCompleteCommand.java`
- [X] T017 [P] [US1] Implement `task delete` command handler in `src/main/java/com/clitaskmanager/cli/TaskDeleteCommand.java`
- [X] T018 [US1] Register task subcommands in main `TaskCommand` router in `src/main/java/com/clitaskmanager/cli/TaskCommand.java`

**Checkpoint**: User Story 1 (Task Lifecycle Management) is fully functional and testable as an MVP.

---

## Phase 4: User Story 2 - Integrated Focus Timer & Session Logging (Priority: P2)

**Goal**: Allow starting and stopping a 25-minute focus timer session linked to a task, logging session history to SQLite.

**Independent Test**: Run `timer start --task-id 1 --duration 25`, query `timer status`, execute `timer stop`, and verify focus time recorded under Task #1 in SQLite.

### Tests for User Story 2
- [X] T019 [P] [US2] Write unit test for `FocusTimerService` timer rules and calculations in `src/test/java/com/clitaskmanager/domain/FocusTimerServiceTest.java`

### Implementation for User Story 2
- [X] T020 [US2] Implement `FocusTimerService` for focus session business logic in `src/main/java/com/clitaskmanager/domain/service/FocusTimerService.java`
- [X] T021 [P] [US2] Implement `timer start` command (25-minute default focus timer) in `src/main/java/com/clitaskmanager/cli/TimerStartCommand.java`
- [X] T022 [P] [US2] Implement `timer status` command in `src/main/java/com/clitaskmanager/cli/TimerStatusCommand.java`
- [X] T023 [P] [US2] Implement `timer stop` command in `src/main/java/com/clitaskmanager/cli/TimerStopCommand.java`
- [X] T024 [US2] Register timer subcommands in `TimerCommand` router in `src/main/java/com/clitaskmanager/cli/TimerCommand.java`

**Checkpoint**: User Stories 1 AND 2 operate independently and in combination.

---

## Phase 5: User Story 3 - Task & Focus Insights (Priority: P3)

**Goal**: Display comprehensive task metadata and focus session history.

**Independent Test**: Execute `task info 1` on a completed task with logged focus sessions and inspect output summary.

### Implementation for User Story 3
- [X] T025 [P] [US3] Implement `task info` command displaying metadata and focus session history in `src/main/java/com/clitaskmanager/cli/TaskInfoCommand.java`

---

## Phase 6: Polish, Error Handling & Architectural Verification

**Purpose**: Cross-cutting CLI error formatting, exit code standard enforcement, and final compliance verification.

- [X] T026 Add central CLI error handling, standard exit code management, and `stderr` formatting in `src/main/java/com/clitaskmanager/cli/CliErrorHandler.java`
- [X] T027 Run end-to-end quickstart validation and verify final implementation against `specs/001-baseline/quickstart.md`, `spec.md`, and `ADR-0001` via `./gradlew test`

---

## Dependencies & Execution Order

### Phase Dependencies
- **Setup (Phase 1)**: Can start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 completion. **BLOCKS all user stories**.
- **User Stories (Phases 3-5)**: All depend on Phase 2 completion.
- **Polish (Phase 6)**: Depends on completion of user stories.

### User Story Sequence
- **US1 (P1)**: Independent, MVP target.
- **US2 (P2)**: Integrates with US1 task IDs, independently testable.
- **US3 (P3)**: Reads US1 and US2 records to display summary insights.

---

## Implementation Strategy

### MVP Scope (User Story 1 Only)
1. Complete Phase 1 (Setup) and Phase 2 (Foundational).
2. Complete Phase 3 (User Story 1 - Task Lifecycle).
3. Validate task CRUD commands and SQLite persistence.

### Full Delivery
1. Add Phase 4 (User Story 2 - Focus Timer).
2. Add Phase 5 (User Story 3 - Insights).
3. Complete Phase 6 (Error Handling & Final Architectural Verification).
