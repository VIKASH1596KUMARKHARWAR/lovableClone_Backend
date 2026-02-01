Lovable Clone – Backend (Spring Boot)
=====================================

A production-grade backend inspired by **AI-assisted collaborative development platforms** (Lovable / Cursor-style systems).

Built using **Spring Boot**, **JPA/Hibernate**, **PostgreSQL**, and **Spring AI**, this backend focuses on:

*   clean domain modeling

*   explicit ownership & authorization

*   transactional correctness

*   project-scoped AI code generation

*   long-lived, auditable AI interactions


Design decisions are intentionally documented to reflect **real-world backend engineering trade-offs**.

High-Level System Overview
--------------------------

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   User   └── Project        ├── Project Members (OWNER / EDITOR / VIEWER)        ├── Chat Session (AI context)        │    ├── Chat Messages        │    └── Chat Events (streamed output)        └── Project Files (AI-generated / user-managed)   `

This backend treats **AI as infrastructure**, not a utility endpoint.

Core Principles (Read This First)
---------------------------------

### 1\. Single Source of Truth

*   **Project ownership and membership live only in project\_members**

*   No duplicated ownership columns on projects

*   All permissions are derived, never inferred


### 2\. Explicitness Over Magic

*   JPA does **not** infer ownership

*   Spring Security does **not** inject business rules

*   Ownership and access are **explicitly resolved in services**


### 3\. Separation of Concerns

*   Controllers → request/response only

*   Services → business logic + authorization

*   Persistence → correctness & constraints

*   AI → isolated generation pipeline


### 4\. Database-Driven Correctness

*   Composite keys model real constraints

*   Soft deletes preserve auditability

*   Invalid state transitions are blocked at the service layer


Features Implemented
--------------------

### Project Management

*   Create projects

*   Update project metadata

*   Fetch user-accessible projects

*   Soft delete projects (non-destructive)


### Ownership & Authorization

*   Each project has **exactly one OWNER**

*   Ownership is defined by:


Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   project_members.project_role = OWNER   `

*   Only the OWNER can:

    *   Update project details

    *   Invite / remove members

    *   Change member roles


Authorization is enforced **inside the service layer**, not controllers.

Ownership Model (Important)
---------------------------

Ownership is modeled as a **role**, not a column.

Why:

*   Prevents ownership duplication

*   Supports future multi-owner policies

*   Keeps collaboration logic centralized

*   Avoids inconsistent state


Domain Model
------------

### Project

Represents a collaborative workspace.

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   @CreationTimestamp  Instant createdAt;  @UpdateTimestamp  Instant updatedAt;  Instant deletedAt; // soft delete   `

*   No owner\_id column

*   Ownership is derived via project\_members


Project Member Service (Collaboration Core)
-------------------------------------------

Collaboration is built on an **invitation-based membership model**.

A user becomes a project member only after:

1.  Being invited by the OWNER

2.  Explicitly accepting the invitation


### ProjectMember Entity

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   @Entity  @Table(name = "project_members")  public class ProjectMember {    @EmbeddedId    ProjectMemberId id;    @ManyToOne    @MapsId("projectId")    Project project;    @ManyToOne    @MapsId("userId")    User user;    @Enumerated(EnumType.STRING)    ProjectRole projectRole;    Instant invitedAt;    Instant acceptedAt;  }   `

This entity is the **single source of truth** for:

*   membership

*   ownership

*   roles

*   invitation lifecycle


### Composite Key (ProjectMemberId)

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   @Embeddable  public class ProjectMemberId {    Long projectId;    Long userId;  }   `

#### Why Composite Keys

*   Prevent duplicate memberships

*   Enforce one user–project relationship

*   Encode real-world constraints at DB level

*   Avoid meaningless surrogate IDs


### Invitation Lifecycle

invitedAtacceptedAtStateNOT NULLNULLPendingNOT NULLNOT NULLActive

No extra status column is required.

### Member Operations

*   **Invite member** (OWNER only)

*   **Accept invitation** (invitee)

*   **Reject invitation** (invitee)

*   **Update role** (OWNER only)

*   **Remove member** (OWNER only)


Invalid transitions (double accept, reject after accept, owner reassignment, etc.) are blocked at the service layer.

Project Roles
-------------

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   public enum ProjectRole {    OWNER,    EDITOR,    VIEWER  }   `

*   **OWNER** – full control

*   **EDITOR** – content modification (feature-dependent)

*   **VIEWER** – read-only access (feature-dependent)


AI-Driven Code Generation (Core Feature)
----------------------------------------

This backend is designed to support **AI-assisted software creation inside projects**.

AI is **project-scoped**, **stateful**, and **auditable**.

AI Architecture Overview
------------------------

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   User Prompt   → Chat Session   → AI Generation Service   → Streamed Events (SSE)   → Chat Messages + Events   → Project Files   `

Key characteristics:

*   AI context is tied to a **project**

*   Every interaction is persisted

*   Output is structured, not plain text


Chat & AI Domain Model
----------------------

### ChatSession

Represents a persistent AI context per user per project.

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   @EmbeddedId  ChatSessionId (projectId, userId)  Instant createdAt;  Instant updatedAt;  Instant deletedAt;   `

Ensures:

*   One AI context per user per project

*   Replayable conversations

*   Long-lived collaboration state


### ChatMessage

Each message exchanged with the AI is stored.

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   @Entity  public class ChatMessage {    @Id    Long id;    @Enumerated(EnumType.STRING)    ChatRole role; // USER, ASSISTANT, SYSTEM, TOOL    String content;    Integer tokensUsed;    Long projectId;    Long userId;  }   `

Used for:

*   Auditing AI behavior

*   Token tracking

*   Billing readiness

*   Replay & debugging


### ChatEvent (Fine-Grained AI Output)

AI responses are decomposed into **events**:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   public enum ChatEventType {    THOUGHT,    MESSAGE,    FILE_EDIT,    TOOL_LOG  }   `

Why events:

*   Enables streaming

*   Supports file diffs

*   Separates reasoning from output

*   Allows structured UI rendering


AI Generation Service
---------------------

AI generation is isolated in a **dedicated service layer**.

Responsibilities:

*   Build prompts using:

    *   Project context

    *   File tree state

    *   Chat history

*   Stream responses token-by-token

*   Parse structured output

*   Persist messages & events

*   Emit file changes


Configured using **Spring AI**:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   spring:    ai:      openai:        base-url: https://openrouter.ai/api        chat:          options:            model: google/gemini-3-flash-preview            temperature: 0.0   `

The provider is **pluggable**.

Streaming AI Responses (SSE)
----------------------------

AI output is delivered using **Server-Sent Events (SSE)**.

Why SSE:

*   Native browser support

*   Low overhead

*   Ideal for token streaming


The backend streams:

*   incremental text

*   structured JSON events

*   file modification instructions


File Generation & Persistence
-----------------------------

AI-generated files are first-class project artifacts.

### ProjectFile Entity

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   @Entity  public class ProjectFile {    @Id    Long id;    String path;    String minioObjectKey;    Long projectId;    Instant createdAt;    Instant updatedAt;  }   `

Design choices:

*   Content stored in **MinIO (S3-compatible)**

*   Database stores metadata only

*   Enables:

    *   versioning

    *   diffs

    *   rollbacks

    *   preview builds


Transaction Management
----------------------

*   All write operations are transactional

*   JPA dirty checking is relied upon

*   Explicit save() avoided when unnecessary


Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   @Transactional  member.setProjectRole(newRole);   `

Soft Delete Strategy
--------------------

Projects are soft-deleted:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   project.setDeletedAt(Instant.now());   `

Benefits:

*   Recoverable data

*   Audit safety

*   Safer than cascading deletes


Error Handling & Validation
---------------------------

*   Centralized @RestControllerAdvice

*   Domain-specific exceptions

*   Consistent API responses

*   DTO-level validation

*   Fail-fast behavior


Tech Stack
----------

*   Java 21

*   Spring Boot

*   Spring Data JPA (Hibernate)

*   Spring AI

*   PostgreSQL

*   MinIO (S3-compatible)

*   Lombok

*   MapStruct


Planned Enhancements (Intentional)
----------------------------------

*   JWT-based method security

*   AI tool calling (search, refactor, test generation)

*   Vector embeddings (semantic file search)

*   Token-based billing enforcement

*   Background AI jobs

*   Agent-based workflows

*   Entity-level soft delete filters


Author
------

**Vikash Kumar Kharwar**B.Tech (CSE)Backend & Full-Stack Developer