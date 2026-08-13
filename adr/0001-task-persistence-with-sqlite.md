# ADR 0001: Task Persistence with SQLite

## Status
Accepted

## Date
2026-08-13

## Context
The CLI Task Manager & Focus Timer application requires a reliable, robust persistent storage mechanism for task data and focus timer logs. The system architecture demands strict separation of concerns, ensuring business and domain logic remain independent of data access and persistence implementations.

## Decision
Use **SQLite** as the exclusive persistent storage mechanism for all task data.

## Constraints
- **SQLite Mandatory**: SQLite MUST be used as the persistent storage engine for all task and focus timer data.
- **No JSON Persistence**: JSON files MUST NOT be used as a substitute for persistent task storage.
- **No In-Memory Substitutes**: In-memory-only storage MUST NOT be used as a substitute for persistent task storage (except as transient test mocks or runtime caches).
- **Separation of Concerns**: Domain and business logic MUST remain decoupled from direct database operations via data access abstraction interfaces (e.g., repository pattern).
- **Architectural Supremacy**: The implementation MUST follow this ADR unless the decision is formally updated or superseded by a subsequent ADR.

## Alternatives Considered

### 1. JSON File Persistence
* **Overview**: Storing tasks as structured JSON records in flat files on disk.
* **Why Rejected**: JSON flat-file storage lacks transactional guarantees, atomic updates, and structured query capabilities. Concurrent access or unhandled process termination can easily cause file corruption or partial writes. As task volumes grow, parsing and re-writing whole JSON documents incurs inefficient file I/O overhead.

### 2. In-Memory-Only Storage
* **Overview**: Holding task state entirely in RAM during process execution without disk persistence.
* **Why Rejected**: In-memory storage fails to meet the core functional requirement of durable task persistence across application launches and command invocations. Data is completely lost upon command exit or system shutdown.

### 3. SQLite (Selected)
* **Overview**: Embeddable, zero-configuration relational database engine providing ACID-compliant transactions in a single file on disk.
* **Why Selected**: SQLite provides complete ACID compliance, atomic operations, crash resilience, efficient indexing, and rich SQL querying capabilities without requiring an external database server daemon. It aligns perfectly with a CLI developer productivity application by running embedded within the application runtime.

## Consequences

### Positive Benefits
- **Data Integrity & Durability**: ACID compliance ensures atomic writes and prevents data corruption during unexpected failures or crashes.
- **Rich Querying Capabilities**: SQL support allows efficient filtering, sorting, indexing, and aggregation of tasks and timer sessions.
- **Zero Administration**: Single-file storage simplifies local backup, migration, and developer workflow without external server management.
- **Architectural Decoupling**: Database interactions can be easily encapsulated behind repository abstractions, isolating persistence mechanics from business logic.

### Trade-offs & Limitations
- **Schema Management**: Requires database migration scripts and schema versioning management as the data model evolves.
- **Binary Format**: Storage file is binary rather than human-editable plain text, requiring SQL CLI tools or application interfaces to inspect data directly.

## Compliance & Constitution Alignment

- **Constitution Alignment**: This ADR serves as the authoritative persistence specification referenced in Principle I (*Architectural Decision Compliance & ADR 0001 Persistence*) and the Architecture & Technology Standards section of `.specify/memory/constitution.md`.
- **Verification & Enforcement**: Compliance will be verified via automated integration tests that inspect the database engine connection, schema migrations, and repository implementations. Code reviews and automated linting/test gates will enforce that no alternative JSON file or in-memory persistence substitutes are introduced into production code paths.
