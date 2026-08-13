# Research & Architectural Decisions: CLI Task Manager & Focus Timer

**Branch**: `001-baseline` | **Date**: 2026-08-13 | **Spec**: [spec.md](./spec.md)

## Overview

This document resolves technical research decisions and architectural choices for implementing the Java/Gradle CLI Task Manager & Focus Timer application in strict compliance with **ADR 0001** and the project **Constitution**.

---

## Technical Decisions & Rationale

### 1. Technology Stack Selection

- **Decision**: Java 21 LTS with Gradle (Kotlin DSL or Groovy DSL build script).
- **Rationale**: Java 21 provides modern language features (record types, pattern matching, virtual threads) ensuring clean, concise code. Gradle provides robust build automation, test execution, and dependency management.
- **Alternatives Considered**:
  - *Python / Click*: Rejected per user technical direction requesting Java + Gradle.
  - *C++ / Go*: Rejected per user technical direction requesting Java + Gradle.

### 2. SQLite Database Driver & Connection Strategy

- **Decision**: Use `org.xerial:sqlite-jdbc` as the SQLite database driver and embedded persistence provider.
- **Rationale**: `sqlite-jdbc` is the industry-standard native SQLite driver for Java. It requires zero external daemon setup and embeds native SQLite binaries for macOS, Linux, and Windows.
- **Database Location**: User home directory configuration path: `~/.cli-task-manager/tasks.db` (configurable via environment variable `TASK_DB_PATH` or CLI `--db` flag).
- **Alternatives Considered**:
  - *JSON File / Jackson Storage*: **REJECTED** per ADR 0001 and Constitution Principle I. Flat-file storage lacks ACID transaction safety and atomic writes.
  - *H2 / Derby*: Rejected because ADR 0001 explicitly mandates SQLite.

### 3. CLI Command Parsing Library

- **Decision**: `info.picocli:picocli` (or lightweight standard Java CLI argument parser).
- **Rationale**: Picocli is a zero-dependency CLI framework for Java with strong support for subcommands (`task add`, `timer start`), type conversion, option validation, and formatted `stdout`/`stderr` output.
- **Alternatives Considered**:
  - *Apache Commons CLI*: Lacks modern subcommand annotations and formatted usage help.
  - *Raw `args[]` parsing*: Prone to fragile argument parsing and boilerplate code.

### 4. Application Architecture & Layering

- **Decision**: Three-layer architecture with strict unidirectional dependencies:
  1. `cli`: Command handlers, Picocli commands, input validation, terminal formatting.
  2. `service` & `domain`: Business logic, state machines, task lifecycle management, timer duration rules.
  3. `persistence`: `TaskRepository` and `FocusSessionRepository` interfaces with `SqliteTaskRepository` and `SqliteFocusSessionRepository` implementations using JDBC.
- **Rationale**: Ensures complete separation of concerns per Constitution Principle II and ADR 0001 constraint 5. Domain models and service logic have zero dependence on JDBC or SQLite classes.

### 5. Focus Timer Session State Management

- **Decision**: Store active focus timer state in SQLite table (`focus_sessions` with status `ACTIVE`) and calculate remaining/elapsed duration dynamically based on `started_at` timestamp and system clock (`Instant.now()`).
- **Rationale**: Dynamic calculation avoids needing a continuous background daemon thread running across CLI command invocations. When the user executes `timer status` or `timer stop`, the system queries the active session record in SQLite and computes elapsed time.

### 6. Testing Strategy & Architectural Compliance Verification

- **Decision**: JUnit 5 (`org.junit.jupiter`) for unit and integration testing.
- **Architectural Compliance Verification**: Automated integration test (`ArchitecturalComplianceTest`) that scans classpath and verifies that:
  1. SQLite JDBC connection is active and creates standard SQLite tables.
  2. No JSON file repositories or flat-file persistence implementations exist in the codebase.
  3. Domain services interact strictly with repository interfaces.

---

## Summary of Resolved Technical Questions

| Research Topic | Chosen Option | Key Rationale |
|----------------|---------------|---------------|
| Language & Runtime | Java 21 LTS | Modern Java features (Records, Sealed Interfaces) |
| Build Tool | Gradle | Standard Java build automation & test execution |
| Persistence Engine | SQLite (`org.xerial:sqlite-jdbc`) | Mandatory per ADR 0001; zero-config single file |
| Command Parser | Picocli (`info.picocli:picocli`) | Subcommand support, clean annotation-driven CLI |
| Testing Framework | JUnit 5 | Native Java testing framework with clean assertions |
| Architecture Pattern | Repository Pattern (3-Tier) | Complete separation of CLI, Business Logic, and Storage |
