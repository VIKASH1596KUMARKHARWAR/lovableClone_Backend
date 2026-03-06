# Lovable Clone – AI App Builder Backend (Spring Boot)

A production-grade backend powering an **AI-driven full-stack application builder**, inspired by platforms like Lovable.

This system enables users to generate and manage complete web applications through AI-assisted workflows — supporting project isolation, role-based collaboration, and subscription-based feature access.

While Lovable focuses on rapid no-code generation, this project concentrates on building the **robust backend architecture required to support AI-generated applications in a production environment**.

The backend is designed to support:

* AI-assisted project scaffolding workflows
* Multi-tenant project isolation
* Strict role-based access control (OWNER / EDITOR / VIEWER)
* Stripe-powered subscription and feature gating
* Secure lifecycle and billing synchronization

Designed with strong emphasis on:

* Explicit ownership & authorization semantics
* Transactional correctness
* Idempotent webhook/event processing
* Security-first API design
* Stripe-driven subscription lifecycle management

---

## Core Principles

### Single Source of Truth

* Project ownership and membership exist **only** in the `project_members` table
* No duplicated ownership columns on the `projects` table
* All access decisions are derived, never inferred

### Explicitness Over Implicit Behavior

* JPA does not infer ownership or permissions
* Spring Security does not inject business rules implicitly
* Authorization is resolved deliberately in the service and security layers

### Separation of Concerns

* Controllers: request/response handling only
* Services: business logic and authorization enforcement
* Persistence layer: data integrity and constraints

### Database‑Driven Correctness

* Composite keys model real‑world constraints
* Soft deletes preserve auditability
* Invalid state transitions are blocked at the service layer

---

## Features Implemented

### Project Management

* Create projects
* Update project metadata
* Fetch user‑accessible projects
* Soft delete projects (non‑destructive)

### Ownership & Authorization

* Each project has **exactly one OWNER**
* Ownership is defined by:

    * `project_members.project_role = OWNER`
* Authorization is enforced at the **service layer**, not controllers

---

## Ownership Model

Project ownership is modeled as a **role**, not a direct column on the project entity.

This ensures:

* A single, consistent source of truth
* No duplicated ownership state
* Clear and testable authorization logic
* Future extensibility (additional roles or policies)

---

## Domain Model

### Project

Represents a collaborative workspace.

* `createdAt` – creation timestamp
* `updatedAt` – last modification timestamp
* `deletedAt` – soft delete marker

Projects do **not** store ownership directly. Ownership is always resolved via `project_members`.

```java
@CreationTimestamp
Instant createdAt;

@UpdateTimestamp
Instant updatedAt;

Instant deletedAt; // soft delete
```

---

## Project Member Service (Collaboration Core)

Collaboration follows an **invitation‑based membership model**.

A user becomes a project member only after:

1. Being invited by the project OWNER
2. Explicitly accepting the invitation

### ProjectMember Entity

```java
@Entity
@Table(name = "project_members")
public class ProjectMember {

  @EmbeddedId
  ProjectMemberId id;

  @ManyToOne
  @MapsId("projectId")
  Project project;

  @ManyToOne
  @MapsId("userId")
  User user;

  @Enumerated(EnumType.STRING)
  ProjectRole projectRole;

  Instant invitedAt;
  Instant acceptedAt;
}
```

This entity is the **single source of truth** for membership, roles, and ownership.

### Composite Key (`ProjectMemberId`)

```java
@Embeddable
public class ProjectMemberId {
    Long projectId;
    Long userId;
}
```

**Why composite keys:**

* Prevent duplicate memberships
* Enforce one user–project relationship
* Encode real‑world constraints at the database level
* Avoid meaningless surrogate identifiers

---

## Invitation Lifecycle

Invitation state is derived purely from timestamps:

| invitedAt | acceptedAt | State   |
| --------- | ---------- | ------- |
| NOT NULL  | NULL       | Pending |
| NOT NULL  | NOT NULL   | Active  |

No additional status column is required.

---

## Project Roles

```java
public enum ProjectRole {
    OWNER,
    EDITOR,
    VIEWER
}
```

* **OWNER** – full control, manages members and lifecycle
* **EDITOR** – content modification within the project
* **VIEWER** – read‑only access

---

## Project Permissions

Permissions represent **capabilities**, independent of roles.

```java
@Getter
@RequiredArgsConstructor
public enum ProjectPermission {

    VIEW("project:view"),
    EDIT("project:edit"),
    DELETE("project:delete"),
    MANAGE_MEMBERS("project:manage_members");

    private final String value;
}
```

### Why Permissions Are Separate From Roles

* Prevents role explosion as features grow
* Allows multiple roles to share capabilities
* Enables future custom roles without controller changes
* Keeps authorization logic explicit and testable

### Role → Permission Mapping (Conceptual)

| Role   | Permissions                        |
| ------ | ---------------------------------- |
| OWNER  | VIEW, EDIT, DELETE, MANAGE_MEMBERS |
| EDITOR | VIEW, EDIT                         |
| VIEWER | VIEW                               |

---

## Project Deletion Rules (Ownership Constraint)

Project deletion follows a **stricter rule** than general permission checks.

Even though the permission model includes a `DELETE` capability, **only the project OWNER is allowed to delete a project itself**.

### Rationale

* `DELETE` permission is reused for multiple destructive actions (files, tasks, comments)
* Deleting a project is a high-impact, lifecycle-level action
* Ownership represents accountability for irreversible operations

### Effective Deletion Matrix

| Role   | Has DELETE Permission | Is OWNER | Can Delete Project |
| ------ | --------------------- | -------- | ------------------ |
| OWNER  | Yes                   | Yes      | Allowed            |
| EDITOR | Yes                   | No       | Forbidden          |
| VIEWER | No                    | No       | Forbidden          |

### Enforcement Strategy

* Project existence and membership are validated first
* Ownership is explicitly checked for deletion
* Non-owners receive **403 FORBIDDEN** (project is visible but action is disallowed)

This preserves:

* Clear ownership semantics
* Reusable permission enums
* Future extensibility without enum changes

---

## Member Role Management Rules

Role and membership management is intentionally **restricted to prevent privilege escalation**.

### Core Rule

* **Only the project OWNER can manage members and change roles**

Editors and viewers are explicitly **not allowed** to:

* Change another member’s role
* Promote themselves or others
* Modify OWNER privileges

### Rationale

* Prevents accidental or malicious privilege escalation
* Centralizes accountability with a single owner
* Matches real-world SaaS authorization models (GitHub, GitLab, Notion)

### Effective Role-Change Matrix

| Actor Role | Target Role | Action Allowed |
| ---------- | ----------- | -------------- |
| OWNER      | Any member  | Yes            |
| EDITOR     | Any member  | No             |
| VIEWER     | Any member  | No             |

### Runtime Behavior

* Editors attempting to change roles receive **403 FORBIDDEN**
* Non-members receive **404 NOT_FOUND**
* Owners can freely manage membership and roles

This ensures:

* No lateral privilege escalation
* No self-promotion paths
* Clear, auditable authorization boundaries

---

## Transaction Management

* All write operations are transactional
* JPA dirty checking is relied upon
* Explicit `save()` calls are avoided when unnecessary

```java
@Transactional
member.setProjectRole(newRole);
```

---

## Soft Delete Strategy

Projects are soft‑deleted using `deletedAt`.

```java
project.setDeletedAt(Instant.now());
```

* No hard deletes
* Data remains recoverable
* Filtering is applied at the query/service layer

---

## Error Handling & Authorization Semantics

* Centralized exception handling via `@RestControllerAdvice`
* Domain‑specific exceptions (`BadRequestException`, `ResourceNotFoundException`, `AccessDeniedException`)
* Consistent API error responses

### 403 vs 404 Semantics

This backend intentionally differentiates **visibility** from **authorization** to prevent information leakage.

| Scenario                              | HTTP Status   |
| ------------------------------------- | ------------- |
| Project does not exist                | 404 NOT_FOUND |
| User is not a project member          | 404 NOT_FOUND |
| User is a member but lacks permission | 403 FORBIDDEN |
| User has required permission          | 200 OK        |

This prevents project ID enumeration and matches real‑world SaaS behavior.

Authorization checks are enforced at the **repository and service layer**, ensuring unauthorized data is never loaded.

---
## Subscription & Billing (Stripe Integration)

This backend includes a **production-grade Stripe subscription system** designed with a strong focus on:

* Provider isolation
* Idempotent webhook handling
* Stripe-as-source-of-truth validation
* Full lifecycle synchronization
* Early trial termination support
* Plan-based feature gating

Stripe is treated strictly as **infrastructure**, not part of the domain model.
Core business logic never depends on Stripe SDK classes.

---

## Design Philosophy

### Stripe Is Infrastructure, Not Domain

* Stripe SDK classes are isolated inside `StripePaymentProcessor`
* Domain services operate only on internal models
* Subscription lifecycle is synchronized exclusively via webhooks
* Business rules are enforced in the service layer

This enables:

* Provider replacement without refactoring business logic
* Testable domain services
* Clean architectural boundaries
* Zero vendor lock-in

---

## High-Level Architecture

```
Controller / Webhook Layer
        ↓
StripePaymentProcessor (Integration Layer)
        ↓
SubscriptionService (Domain Layer)
        ↓
JPA Entities & Repositories
```

---

## Checkout Flow

### Client Request

`POST /api/payment/checkout`

### Backend Validation

Before creating a Stripe Checkout Session:

* User exists
* Plan exists
* No blocking Stripe subscription exists

Stripe is queried directly to validate subscription state.

### Metadata Embedding

* `userId`
* `planId`

Stripe-side validation ensures:

* Users cannot bypass subscription limits
* Database inconsistencies do not allow duplicate subscriptions

---

## Trial Management (Early Termination Supported)

The system supports both automatic trial expiration and manual early trial termination.

### Manual Early Trial Termination

Endpoint:

`POST /api/subscription/end-trial`

Behavior:

* Validates user has a TRIALING subscription
* Calls Stripe API with `trial_end = NOW`
* Stripe immediately:

    * Ends trial
    * Generates invoice
    * Attempts payment
    * Transitions subscription to ACTIVE
* Webhook synchronizes updated state to database

This ensures:

* Stripe remains lifecycle authority
* Billing is triggered immediately
* No direct DB mutation occurs without webhook confirmation

---

## Stripe Validation Strategy (Production-Safe)

Before creating checkout, Stripe is queried for subscriptions in:

* ACTIVE
* TRIALING
* PAST_DUE

Subscriptions scheduled for cancellation (`cancel_at != null`) **do not block checkout**.

This ensures:

* Stripe is the single source of truth
* No duplicate active subscriptions
* No DB-driven lifecycle corruption

---

## Webhook Lifecycle Handling

All subscription state changes are driven exclusively by Stripe webhooks.

### Handled Events

* `checkout.session.completed`
* `customer.subscription.updated`
* `customer.subscription.deleted`
* `invoice.paid`
* `invoice.payment_failed`
* `customer.subscription.trial_will_end`

---

## Subscription Lifecycle Coverage

### 1️⃣ Creation

Event: `checkout.session.completed`

* Subscription activated in database
* Stripe subscription ID persisted

---

### 2️⃣ Plan Upgrade / Downgrade

Event: `customer.subscription.updated`

* Stripe price ID mapped to internal Plan
* Plan updated
* Billing period timestamps synchronized

---

### 3️⃣ Scheduled Cancellation (End of Period)

Stripe may send:

* `cancel_at_period_end = true`
  OR
* `cancel_at != null`

Both cases are mapped to:

* `cancelAtPeriodEnd = true`
* `status = ACTIVE`

The subscription remains active until the billing period ends.

---

### 4️⃣ Immediate Cancellation

Triggered via:

```
stripe subscriptions cancel sub_xxx
```

Stripe emits:

`customer.subscription.deleted`

Database updates:

* `status = CANCELLED`
* `canceledAt` populated
* `endedAt` populated
* `cancelAtPeriodEnd = false`

---

### 5️⃣ Early Trial Conversion

Triggered via:

`POST /api/subscription/end-trial`

Stripe emits:

* `customer.subscription.updated`
* `invoice.created`
* `invoice.paid`

Database transitions:

* `TRIALING → ACTIVE`
* Billing period timestamps updated
* Trial timestamps preserved for audit

---

### 6️⃣ Payment Failure

Event: `invoice.payment_failed`

Subscription status becomes:

* `PAST_DUE`

---

## Idempotent Webhook Processing

Stripe may retry events.

Duplicate subscription creation is prevented via:

```java
if (subscriptionRepository.existsByStripeSubscriptionId(subscriptionId)) {
    return;
}
```

This guarantees **exactly-once activation semantics**.

---

## Domain Model

### Subscription Entity

Key fields:

* `stripeSubscriptionId`
* `stripeCustomerId`
* `status`
* `currentPeriodStart`
* `currentPeriodEnd`
* `cancelAtPeriodEnd`
* `canceledAt`
* `endedAt`
* `trialStart`
* `trialEnd`
* `latestInvoiceId`

The database mirrors Stripe state but never drives lifecycle decisions.

---

## Feature Gating Logic

Project creation limits are enforced at the service layer.

Behavior:

* FREE users → limited to 1 project
* TRIAL users → configurable restriction (e.g., 1 project)
* ACTIVE users → governed by `plan.maxProjects`
* Unlimited plans supported (`maxProjects = null`)

This ensures clean separation between billing state and product rules.

---

## Security

* Webhook signatures verified via `Webhook.constructEvent`
* Public webhook endpoint secured cryptographically
* JWT-based stateless authentication
* Stripe API keys and webhook secrets stored in environment variables

---

## Architectural Guarantees

This billing layer guarantees:

* Stripe is the authoritative subscription source
* No duplicate active subscriptions
* Full support for:

    * Immediate cancellation
    * Scheduled cancellation
    * Plan upgrades / downgrades
    * Early trial conversion
    * Past due handling
    * Webhook idempotency
* Provider replaceability

---

## Consistency & Concurrency Guarantees

The system safely handles race conditions and concurrent webhook deliveries.

### Database Constraints

* `stripeSubscriptionId` is unique
* Active subscription enforcement at service layer
* Idempotent creation logic prevents duplicate rows

### Transactional Safety

All subscription state changes execute inside transactional service methods to ensure:

* Atomic state transitions
* No partial updates
* Safe retry behavior

---

## Stripe as Lifecycle Authority

Stripe acts as the authoritative subscription state machine.

The database:

* Mirrors Stripe state
* Stores lifecycle timestamps for auditing
* Does not initiate lifecycle transitions independently

All lifecycle transitions are webhook-driven.

---

## State Transition Guarantees

Stripe status → Internal status mapping:

* `active` → ACTIVE
* `trialing` → TRIALING
* `past_due` → PAST_DUE
* `canceled` → CANCELLED
* `incomplete` → INCOMPLETE

Unknown states are logged and handled safely.

---

## Webhook Ordering & Retry Strategy

Stripe may:

* Retry events
* Deliver events out of order
* Deliver duplicate events

Handled via:

* Idempotent guards
* Full subscription synchronization on update events
* Safe transactional updates

Webhook handlers never assume event uniqueness.

---

## Production Readiness

The billing system has been validated against:

* Create → Trial → Early Trial End → Active
* Create → Upgrade → Cancel → Resume → Cancel
* Immediate cancellation flow
* Scheduled cancellation flow
* Payment failure flow
* Webhook retry scenarios

Lifecycle correctness is enforced via:

* Explicit status mapping
* Transactional service methods
* Stripe-driven state synchronization

---
## AI Code Generation Engine

This backend includes a production-ready AI orchestration layer responsible for:

- Structured LLM prompt construction
- Streaming responses via SSE
- Tool-calling (file retrieval)
- Guardrail-enforced deterministic XML output
- MinIO-backed file persistence
- Circuit breaker protection

Full Detailed Documentation:

       AI Code Generation Engine → docs/AI_CODE_GENERATION_ENGINE.md

## Architecture Notes

* Controllers remain thin
* Business logic lives in services
* Authorization is explicit and testable
* Design favors correctness and clarity over premature optimization
* Stripe SDK isolated to integration layer only
---

## Author
**Vikash Kumar Kharwar**
B.Tech CSE
Backend & Full‑Stack Developer

