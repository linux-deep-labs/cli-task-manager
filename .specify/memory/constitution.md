<!--
Sync Impact Report:
- Version change: Initial creation → 1.0.0
- List of modified principles:
  - Initialized Core Principle I: Architectural Decision Compliance & ADR 0001 Persistence
  - Initialized Core Principle II: Separation of Concerns & Clean Layering
  - Initialized Core Principle III: Code Quality, Simplicity & Automated Testability
  - Initialized Core Principle IV: Spec & Technical Plan Alignment
  - Initialized Core Principle V: Verifiable Implementation & Automated Compliance
- Added sections:
  - Core Principles
  - Architecture & Technology Standards
  - Governance
- Removed sections: None
- Follow-up TODOs: None
-->

# CLI Task Manager & Focus Timer Constitution

## Core Principles

### I. Architectural Decision Compliance & ADR 0001 Persistence
Architectural decisions MUST be formally documented and strictly followed. ADR 0001 is the
authoritative architectural decision for task persistence. The application MUST use SQLite for
persistent task storage. JSON files, flat files, and in-memory-only storage MUST NOT be used as
substitutes for SQLite persistence.

### II. Separation of Concerns & Clean Layering
Business logic MUST be strictly separated from persistence concerns. Data access abstractions and
domain logic MUST remain decoupled from database implementation details to ensure modularity
and clean system boundaries.

### III. Code Quality, Simplicity & Automated Testability
Code MUST be simple, readable, maintainable, and testable. All core task management behavior,
focus timer logic, and domain workflows MUST be covered by appropriate automated tests.

### IV. Spec & Technical Plan Alignment
Implementation MUST strictly align with the approved feature specification (`spec.md`) and
technical plan (`plan.md`). Features or structural modifications outside the approved design
artifacts MUST NOT be introduced without formal spec updates.

### V. Verifiable Implementation & Automated Compliance
The final implementation MUST be verifiable against the specification and active ADRs. Automated
checks and test suites MUST be used where practical to validate contract adherence and system
correctness.

## Architecture & Technology Standards

- **Task Persistence Standard**: SQLite is mandated per ADR 0001 as the sole persistent data store
  for tasks. Flat files, JSON storage, and in-memory-only stores MUST NOT be used for persistence.
- **Architectural Change Control**: AI coding agents and human developers MUST NOT make
  architecturally significant changes without updating or superseding the relevant ADR.

## Governance

- **Human Review of Architectural Changes**: All proposed architectural revisions, structural
  modifications, or ADR updates require human review and explicit approval.
- **AI-Assisted Development**: AI coding agents MUST operate within the boundaries of the approved
  constitution, ADRs, specifications, and plans.
- **ADR Updates for Significant Changes**: Any significant architectural change MUST be formally
  documented in a new or updated Architectural Decision Record (ADR) prior to implementation.
- **Compliance with Spec & Plan**: All implementation work MUST be verified for complete
  compliance with the approved feature specification (`spec.md`) and technical plan (`plan.md`).

**Version**: 1.0.0 | **Ratified**: 2026-08-13 | **Last Amended**: 2026-08-13
