# Lovable Clone – AI Code Generation Engine

## (Persona Project – AI + System Design + Backend Architecture)

---

##  Project Overview

This project represents the **AI Code Generation Engine** of a Lovable.dev–style platform.

It enables users to modify real codebases using natural language instructions such as:

> "Update the color of ProfileCard button to Red"

The system intelligently:

* Fetches the project structure
* Injects relevant file content
* Applies guardrails and constraints
* Calls the LLM with structured context
* Streams the response in real time
* Parses structured file updates
* Stores metadata and files safely

This is not a simple LLM wrapper — it is a **backend AI orchestration layer** designed for production readiness.

---

#  High-Level Architecture
![AI Code Generation Architecture](./ai_design_architecture.png)
## 1. Frontend (React)

Responsibilities:

* Sends user prompt via POST request
* Receives streaming LLM response
* Displays assistant message in real-time
* Applies returned file updates after parsing

The frontend behaves like a live AI-powered code editor.

---

## 2. Spring Boot Backend (AI Orchestrator)

The backend acts as the **core AI orchestration layer**.

Responsibilities:

* Accept user prompt
* Inject system prompt (rules, constraints, guardrails)
* Fetch project file tree
* Call tools when required
* Construct final LLM input
* Stream LLM response
* Buffer chunks safely
* Parse structured XML-style output
* Store file content in MinIO
* Store metadata in database

The LLM is treated as infrastructure — not business logic.

---

## 3. Prompt Construction Strategy

Final LLM Input Context:

User Prompt
+ System Prompt
+ File Tree
+ Selected File Content

This ensures:

* Context awareness
* Controlled generation
* Deterministic updates
* Reduced hallucinations
* Token-efficient injection

---

#  Tooling Layer

### Tool: `get_file_content(paths[])`

Used when the LLM determines that specific files are required.

Flow:

1. LLM identifies required file paths
2. LLM calls tool
3. Backend validates and fetches file content
4. Content is injected back into model context

Protected with:

* Circuit breaker
* Rate control
* Path validation

---

#  Structured LLM Output Enforcement

The LLM must return structured XML-style output:

```
<message>
Assistant explanation here
</message>

<file name="src/App.tsx">
Updated code here
</file>
```

### Why Structured Output?

- Prevents JSON corruption
- Deterministic parsing
- Clean separation of assistant message and file updates
- Production-safe application of changes

Free-form responses are never allowed.

---

#  Streaming Architecture

    LLM → Stream chunks → Backend → Frontend

### Backend:

* Buffers chunks using StringBuilder
* Tracks token usage information
* Parses final response after stream completion
* Extracts assistant message
* Extracts file updates

### Frontend:

* Displays assistant message live
* Applies file updates once fully parsed

Streaming-first design improves UX and perceived performance.

---

#  Storage Architecture (MinIO + DB)

| Data Type      | Storage Location |
| -------------- | ---------------- |
| File content   | MinIO bucket     |
| Metadata       | Database         |
| Template files | Template bucket  |

### Why This Design?

* Scalable object storage
* Large file handling
* Clean infra separation
* Replaceable storage backend

---

# Guardrails & System Constraints

The system prompt enforces:

* No hallucinated files
* No unsafe code
* Modify only requested files
* Always return structured XML output
* Follow existing project conventions

This ensures safe and predictable AI behavior.

---

#  Design Philosophy

## 1. LLM Is Infrastructure

The LLM is treated as:

* A replaceable component
* An external dependency
* Not part of domain logic

No domain layer depends directly on SDK-specific classes.

---

## 2. File System Is Context, Not State

We:

* Inject only required files
* Avoid sending entire project
* Optimize token usage

---

## 3. Deterministic Parsing Only

All AI responses must be:

* Wrapped
* Structured
* Parseable
* Deterministic

---

## 4. Circuit Breaker Stability
If:

* Tool fails
* LLM timeouts are handled gracefully
* MinIO is slow

System degrades gracefully.

---

#  Key Strengths

* Streaming-first architecture
* Tool-calling capable
* Structured deterministic parsing
* Object-storage optimized
* Token-aware context construction
* Guardrail enforced
* Infra-isolated LLM integration
* Multi-tenant scalable design ready

---

#  Scalability Roadmap

Planned production upgrades:

* File diff-based updates instead of full overwrite
* Versioning per AI generation
* Token budgeting system
* File tree caching
* Multi-model routing
* Embedding-based semantic retrieval
* Background job queue for large tasks
* Project snapshotting

---

