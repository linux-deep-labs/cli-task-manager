# Quickstart Validation Guide: CLI Task Manager & Focus Timer

**Branch**: `001-baseline` | **Date**: 2026-08-13 | **Spec**: [spec.md](./spec.md) | **ADR**: [ADR-0001](../../adr/0001-task-persistence-with-sqlite.md)

## Prerequisites

- Java 21 LTS installed (`java -version`)
- Gradle installed (`gradle -v`) or Gradle Wrapper (`./gradlew`)

---

## 1. Building the Application

```bash
# Build the executable shadow JAR / application CLI
./gradlew build
```

---

## 2. End-to-End Validation Scenarios

### Scenario A: Task CRUD Operations & SQLite Verification

```bash
# 1. Create a task
./gradlew run --args="task add 'Implement database layer' --priority HIGH --due 2026-08-30"

# 2. List tasks
./gradlew run --args="task list"

# 3. View task details
./gradlew run --args="task info 1"

# 4. Mark task completed
./gradlew run --args="task complete 1"

# 5. Verify SQLite database file created and non-empty
ls -l ~/.cli-task-manager/tasks.db
sqlite3 ~/.cli-task-manager/tasks.db "SELECT * FROM tasks;"
```

### Scenario B: Focus Timer 25-Minute Session Flow

```bash
# 1. Create a task for focus session
./gradlew run --args="task add 'Refactor domain service' --priority MEDIUM"

# 2. Start 25-minute focus session linked to task #2
./gradlew run --args="timer start --task-id 2 --duration 25"

# 3. Check timer status
./gradlew run --args="timer status"

# 4. Stop timer session
./gradlew run --args="timer stop"

# 5. Verify focus session logged in SQLite
sqlite3 ~/.cli-task-manager/tasks.db "SELECT * FROM focus_sessions;"
```

---

## 3. Automated Test & Architectural Compliance Verification

```bash
# Execute unit tests, integration tests, and architectural compliance tests
./gradlew test
```
