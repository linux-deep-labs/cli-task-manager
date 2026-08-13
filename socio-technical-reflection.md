# Socio-Technical Reflection: CLI Task Manager & Focus Timer

**Date**: 2026-08-13
**Project**: CLI Task Manager & Focus Timer
**Governance Stack**: Spec Kit (`.specify`), ADR 0001, Project Constitution

---

## Executive Summary

The **CLI Task Manager & Focus Timer** was developed as a Greenfield developer productivity tool that provides task management and a fixed 25-minute Pomodoro focus timer through a command-line interface.

Beyond the technical implementation, the project demonstrates a socio-technical approach to software architecture in which architectural decisions, specifications, AI-assisted implementation, automated testing, and human governance work together.

The project uses **ADR 0001** to establish SQLite as the persistence strategy. This decision is carried through the Spec-Driven Development workflow and verified through automated architectural tests and the `/speckit.converge` process.

---

## 1. Socio-Technical Context & Human-Centered Productivity

Developers frequently work with multiple tasks and interruptions while using terminal-based development environments. A CLI-based productivity tool can reduce the need to switch between different applications when managing development tasks and focus sessions.

The system addresses this context through two primary capabilities:

- **Task Management**: Users can add, list, update, complete, delete, and inspect tasks directly from the terminal.
- **Focus Management**: Users can start, inspect, and stop a fixed 25-minute Pomodoro focus session.

The CLI provides a low-friction interface that keeps task management and focus tracking within the developer's existing terminal workflow.

The application also avoids requiring a separate database server. SQLite provides local persistence while keeping the application self-contained and appropriate for a Greenfield CLI application.

---

## 2. Governance, Architectural Constraints & ADR 0001

A major architectural concern in software development is **architectural erosion**, where implementation gradually diverges from the original architectural intent because of convenience, changing requirements, or decisions made without documentation.

This project addresses that problem through a layered governance model:

```text
+-------------------------------------------------------------------+
|                        Project Constitution                       |
|                  Project-level architectural rules                |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                           ADR 0001                                |
|                  SQLite Task Persistence                          |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                 Architectural Fitness Function                    |
|                  ArchitecturalComplianceTest                     |
+-------------------------------------------------------------------+
```

### The SQLite Decision

ADR 0001 established the following architectural decision:

> **Use SQLite for task persistence.**

Three persistence strategies were considered:

1. Local JSON files
2. SQLite
3. In-memory storage

SQLite was selected because it provides persistent, structured, relational storage without requiring a separate database server.

The decision also provides a foundation for future features such as task filtering, priorities, timestamps, and additional relationships.

The Constitution and implementation plan reinforced this decision by explicitly requiring the application to use SQLite rather than replacing persistence with JSON files or in-memory-only storage.

### Separation of Concerns

The implementation separates domain concepts from persistence implementation through repository interfaces.

For example:

```text
Task
FocusSession
    ↓
TaskRepository
FocusSessionRepository
    ↓
SqliteTaskRepository
SqliteFocusSessionRepository
    ↓
SQLite
```

This separation allows the domain layer to remain independent of SQLite-specific implementation details.

---

## 3. Human-AI Collaboration via Spec Kit

The project used a structured Spec-Driven Development workflow rather than allowing implementation to begin from unstructured prompts.

The workflow was:

```text
ADR 0001
    ↓
/speckit.constitution
    ↓
/speckit.specify
    ↓
/speckit.plan
    ↓
/speckit.tasks
    ↓
/speckit.implement
    ↓
/speckit.converge
```

### Key Dynamics Observed

#### Architectural Guardrails

The Constitution and ADR established constraints before implementation began.

In particular, ADR 0001 required SQLite for task persistence. This prevented the persistence strategy from being selected implicitly during implementation.

During `/speckit.implement`, the resulting implementation included:

- `DatabaseConnectionManager`
- `SqliteTaskRepository`
- `SqliteFocusSessionRepository`
- SQLite database migration scripts

This demonstrates that the architectural decision was translated into concrete implementation components.

#### Traceability

The implementation was organized around the task list generated by Spec Kit.

The implementation phase reported:

```text
T001 – T027
27 / 27 tasks completed
```

This provided traceability between the planned work and the generated implementation.

#### AI as an Implementation Assistant

The AI coding agent was used primarily as an implementation assistant. Architectural intent was defined before code generation through the Constitution, ADR, Specification, Plan, and Tasks.

This creates a human-in-the-loop development model in which the human defines and governs intent while the AI assists with implementation.

---

## 4. Closed-Loop Correction with `/speckit.converge`

The `/speckit.converge` command provided the final verification stage of the Spec-Driven Development workflow.

The convergence process checked the implementation against the project Constitution, ADR 0001, functional requirements, success criteria, user stories, and implementation tasks.

The final results were:

| Verification Area                   |       Result |
| ----------------------------------- | -----------: |
| Constitution Principles             |   5/5 passed |
| ADR-0001 Constraints                |   5/5 passed |
| Functional Requirements             | 18/18 passed |
| Success Criteria                    |   4/4 passed |
| User Stories & Acceptance Scenarios |   3/3 passed |
| Actionable Tasks                    | 27/27 passed |
| Missing Findings                    |            0 |
| Partial Findings                    |            0 |
| Contradictions                      |            0 |
| Unrequested Changes                 |            0 |
| Architectural Drift                 |            0 |

The final outcome was:

> **Converged — The implementation fully satisfies the specification, technical plan, and task list in complete compliance with ADR-0001 and the project Constitution.**

No architectural drift was identified, so no additional implementation tasks were required.

This demonstrates the value of a closed-loop process. Instead of assuming that the generated implementation was correct, the final implementation was systematically checked against the original architectural and functional intent.

---

## 5. Human-in-the-Loop Governance

AI coding agents can accelerate implementation, but they should not independently determine significant architectural decisions.

A tool such as **Decision Guardian** could assist human reviewers by connecting Pull Requests with relevant Architecture Decision Records.

For example, if a developer or AI agent attempted to replace SQLite with JSON persistence, the governance tool could identify ADR 0001 as the relevant architectural decision and surface it to the reviewer.

A possible governance workflow is:

```text
Developer / AI Agent
        ↓
Pull Request
        ↓
Decision Guardian
        ↓
Relevant ADR
        ↓
Human Architectural Review
        ↓
Approve / Request Changes
```

This approach keeps architectural authority with the human reviewer while allowing AI agents to accelerate implementation.

If the persistence strategy needs to change in the future, the existing ADR should be explicitly superseded by a new architectural decision before the implementation is changed.

---

## 6. Automated Fitness Function

The project includes an automated architectural fitness function through:

```text
ArchitecturalComplianceTest.java
```

The purpose of this test is to verify that the persistence implementation remains aligned with ADR 0001.

The architectural constraint can be summarized as:

```text
Task persistence MUST use SQLite.

JSON files MUST NOT be used as the primary
task persistence mechanism.

In-memory-only storage MUST NOT replace
SQLite persistence.
```

The test verifies the SQLite persistence infrastructure, including the SQLite JDBC connection and database initialization.

This converts an architectural rule from documentation into an executable verification mechanism.

The governance model is therefore:

```text
ADR 0001
   ↓
Architectural Constraint
   ↓
Implementation
   ↓
ArchitecturalComplianceTest
   ↓
Automated Test Verification
```

If future implementation changes violate the architectural constraint, the architectural compliance test provides an automated mechanism for detecting the violation.

---

## 7. System Outcomes

| Dimension        | Target                                  | Realized Outcome                                                        |
| ---------------- | --------------------------------------- | ----------------------------------------------------------------------- |
| Architecture     | Layered Java CLI application            | Separation between CLI, domain, and persistence                         |
| Persistence      | SQLite according to ADR 0001            | SQLite repositories and database connection management                  |
| Task Management  | Terminal-based task operations          | Add, list, update, complete, delete, and info commands                  |
| Focus Management | Fixed 25-minute Pomodoro                | Start, status, and stop timer commands                                  |
| Testing          | Verify system behavior and architecture | Unit tests, SQLite integration tests, and architectural compliance test |
| Governance       | Prevent architectural drift             | ADR 0001, Constitution, and `/speckit.converge` verification            |

---

## Conclusion

The **CLI Task Manager & Focus Timer** demonstrates how software architecture can be governed through explicit decisions, specifications, automated verification, and human review.

ADR 0001 established SQLite as the persistence strategy before implementation began. The decision was then propagated through the Spec-Driven Development workflow and reflected in the generated implementation.

The `/speckit.implement` phase completed all **27 implementation tasks**, while the final `/speckit.converge` process confirmed that the implementation satisfied the Constitution, ADR 0001, functional requirements, success criteria, user stories, and task list.

Most importantly, the project demonstrated that an ADR can function as an architectural guardrail rather than merely serving as documentation. The addition of `ArchitecturalComplianceTest` further converts the architectural decision into an executable verification mechanism.

The main lesson from this assignment is that modern software architecture is not only about choosing technologies. It is also about documenting intent, governing AI-assisted implementation, maintaining traceability, and continuously verifying that the implemented system remains aligned with the architectural decisions that define it.
