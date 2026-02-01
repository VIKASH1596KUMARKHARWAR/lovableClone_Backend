# Lovable Clone – Backend (Spring Boot)

A **production-grade backend** inspired by **AI-assisted collaborative development platforms** (Lovable / Cursor-style systems).

Built using **Spring Boot**, **JPA/Hibernate**, **PostgreSQL**, and **Spring AI**, this backend focuses on:

- clean domain modeling
- explicit ownership & authorization
- transactional correctness
- project-scoped AI code generation
- long-lived, auditable AI interactions

All design decisions are intentionally documented to reflect **real-world backend engineering trade-offs** and to give reviewers full clarity on system intent, scope, and architecture.

---
## Design Documentation

Detailed design decisions and architecture notes are available in the `docs/` directory:

- Architecture & service boundaries
- AI generation pipeline
- Domain modeling decisions
- Planned system evolution
- 

## AI Architecture Overview

<p align="center">
  <img src="doc/code_execution_system_architecture.png"
       alt="AI Code Execution System Architecture"
       width="800">
</p>

*High-level execution flow showing how project-scoped AI generation,
streaming, persistence, and file creation are handled in the backend.*
--- 

## High-Level System Overview

## High-Level System Overview

```text
User
└── Project
    ├── Project Members (OWNER / EDITOR / VIEWER)
    ├── Chat Session (AI context)
    │   ├── Chat Messages
    │   └── Chat Events (streamed output)
    └── Project Files (AI-generated / user-managed)



This backend treats AI as infrastructure, not a stateless chat endpoint.
```
---

## Core Principles (Read This First)

### Single Source of Truth
- Project ownership and membership exist **only** in `project_members`
- No duplicated ownership column on `projects`
- All permissions are derived, never inferred

### Explicitness Over Magic
- JPA does **not** infer ownership
- Spring Security does **not** inject business rules
- Ownership, access, and AI scope are **explicitly resolved in the service layer**

### Separation of Concerns
- Controllers → request / response only
- Services → business logic + authorization
- Persistence → correctness & constraints
- AI → isolated generation pipeline

### Database-Driven Correctness
- Composite keys encode real-world constraints
- Soft deletes preserve auditability
- Invalid state transitions are blocked at the service layer

---

## Features Implemented

### Project Management
- Create projects
- Update project metadata
- Fetch user-accessible projects
- Soft delete projects (non-destructive)

### Ownership & Authorization
- Each project has **exactly one OWNER**
- Ownership is defined by:



project_members.project_role = OWNER


Only the OWNER can:
- Update project details
- Invite / remove members
- Change member roles

Authorization is enforced **inside the service layer**, not controllers.

---

## Ownership Model

Ownership is modeled as a **role**, not a column.

Why:
- Prevents ownership duplication
- Enables future multi-owner or org-level models
- Keeps collaboration logic centralized
- Avoids inconsistent state

---

## Domain Model

### Project
Represents a collaborative workspace.

- `createdAt` – creation timestamp
- `updatedAt` – last modification timestamp
- `deletedAt` – soft delete marker

Projects **do not store ownership directly**.  
Ownership is derived via `project_members`.

---

## Project Member Service (Collaboration Core)

Collaboration follows an **invitation-based membership model**.

A user becomes a project member only after:
1. Being invited by the OWNER
2. Explicitly accepting the invitation

### ProjectMember
The `project_members` table is the **single source of truth** for:
- membership
- ownership
- roles
- invitation lifecycle

Each record contains:
- composite key `(projectId, userId)`
- role (OWNER / EDITOR / VIEWER)
- invitation timestamps

### Composite Key (ProjectMemberId)

Why composite keys:
- Prevent duplicate memberships
- Enforce one user–project relationship
- Model real-world constraints at DB level
- Avoid meaningless surrogate IDs

---

## Invitation Lifecycle

Invitation state is derived purely from timestamps:

| invitedAt | acceptedAt | State   |
|---------|-----------|--------|
| NOT NULL | NULL      | Pending |
| NOT NULL | NOT NULL  | Active  |

No additional status column is required.

---

## Project Member Operations

- Invite member (OWNER only)
- Accept invitation (invitee)
- Reject invitation (invitee)
- Update member role (OWNER only)
- Remove member (OWNER only)

All invalid transitions (double accept, reject after accept, owner reassignment, etc.) are blocked at the service layer.

---

## Project Roles

- **OWNER** – full control
- **EDITOR** – content modification (feature-dependent)
- **VIEWER** – read-only access

---

## AI-Driven Code Generation (Core Feature)

This backend supports **AI-assisted software creation inside projects**.

AI is:
- project-scoped
- stateful
- auditable

AI is treated as a **first-class backend capability**, not a utility chat endpoint.

---

## AI Architecture Overview


User Prompt
→ Chat Session
→ AI Generation Service
→ Streamed Events (SSE)
→ Chat Messages + Events
→ Project Files




Key characteristics:
- AI context is tied to a project
- Every interaction is persisted
- Output is structured, not plain text

---

## Chat & AI Domain Model

### ChatSession
- One session per `(projectId, userId)`
- Persistent AI context
- Enables replayable conversations
- Supports long-lived collaboration state

### ChatMessage
Stores each interaction with the AI.

Used for:
- auditing AI behavior
- token usage tracking
- billing readiness
- debugging and replay

### ChatEvent
AI output is decomposed into fine-grained events:
- THOUGHT
- MESSAGE
- FILE_EDIT
- TOOL_LOG

Why events:
- Enables streaming
- Supports file diffs
- Separates reasoning from output
- Allows deterministic UI rendering

---

## AI Generation Service

AI generation is isolated in a **dedicated service layer**.

Responsibilities:
- Build prompts using project context, file tree, and chat history
- Stream responses token-by-token
- Parse structured output
- Persist messages and events
- Emit file creation / modification instructions

Configured via **Spring AI** with a pluggable provider (OpenRouter / OpenAI / Gemini).

---

## Streaming AI Responses (SSE)

AI output is delivered using **Server-Sent Events (SSE)**.

Why SSE:
- Native browser support
- Low latency
- Ideal for incremental AI output

The backend streams:
- incremental text
- structured JSON events
- file modification instructions

---

## File Generation & Persistence

AI-generated files are **first-class project artifacts**.

Design choices:
- File content stored in **MinIO (S3-compatible storage)**
- Database stores metadata only
- Enables versioning, diffs, rollbacks, and previews

---

## Transaction Management

- All write operations are transactional
- JPA dirty checking is relied upon
- Explicit `save()` calls avoided when unnecessary

---

## Soft Delete Strategy

Projects are soft-deleted using `deletedAt`.

Benefits:
- Recoverable data
- Audit safety
- Safer than cascading deletes

---

## Error Handling & Validation

- Centralized `@RestControllerAdvice`
- Domain-specific exceptions
- Consistent API error responses
- DTO-level validation
- Fail-fast behavior

---

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA (Hibernate)
- Spring AI
- PostgreSQL
- MinIO (S3-compatible)
- Lombok
- MapStruct

---

## Future Architecture Evolution (Planned)

This backend is currently implemented as a **modular monolith** by design.

The domain boundaries (Projects, Members, AI, Files, Billing) are intentionally isolated
to allow a smooth transition to a distributed architecture when scale demands it.

Planned evolution includes:

### Monolith → Microservices
- Project Service
- Membership / Authorization Service
- AI Generation Service
- File Service
- Billing & Usage Service

Each service boundary already exists conceptually at the service layer.

### Event-Driven Architecture (Kafka)
- ProjectCreated / ProjectDeleted
- MemberInvited / MemberAccepted
- AIGenerationRequested / Completed
- TokenUsageRecorded

Kafka will be used for:
- async processing
- billing
- notifications
- AI background jobs

### Kubernetes & Cloud-Native
- Containerized deployment (Docker)
- Horizontal scaling via Kubernetes
- Config & secrets via Spring Cloud Config / Vault
- Observability via metrics & tracing

This staged approach avoids premature complexity while keeping the system
**migration-ready**.


---

## Author

**Vikash Kumar Kharwar**  
B.Tech (CSE)  
Backend & Full-Stack Developer

---

## Note

This project is under active development.  
The README deliberately documents architecture, intent, and trade-offs to give external reviewers a **clear, honest, and professional understanding** of the system.
