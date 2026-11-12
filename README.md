# 🏦 Loan Payment Processing API (Bank Transfer Processing Engine)

> A financial processing engine designed under **Hexagonal Architecture** and **Domain-Driven Design (DDD)** principles, prioritizing atomic consistency, idempotency, and absolute traceability.

---

## 🧠 Critical Architectural Decisions

### 1. Money as a Value Object (`Money`)
We strictly avoid `Double` or `Float` for financial values. Instead, we implemented a **`Money` Value Object** using `BigDecimal` with a fixed scale (2 decimal places) and **Banker's Rounding** (`HALF_EVEN`).
* **Reason:** Prevent accumulated decimal precision errors that lead to accounting discrepancies in balances.

### 2. Consistency via Optimistic Locking
Bank accounts utilize a `@Version` field for **Optimistic Locking**.
* **Reason:** In high-concurrency environments, this ensures that two simultaneous transfers do not corrupt the same account balance, throwing a controlled `ObjectOptimisticLockingFailureException` during collisions.

### 3. Duplicate Prevention via Idempotency Key (Database Constraint)
Every transfer requires a unique `idempotencyKey`.
* **Reason:** In distributed environments, network timeouts often lead to client retries. Enforcing a **Unique Constraint** at the database level prevents duplicate transfer records from being processed and persisted for the same operation key.

### 4. Immutable Ledger (Audit Trail)
Transactions are never edited or deleted. They are **Append-Only** records.
* **Reason:** Compliance with banking audit standards. Any error is corrected with a reversal or adjustment transaction, keeping the historical record intact for reconciliation.

---

* **Account** is modeled as the Aggregate Root responsible for protecting balance invariants.
* **Transfers** coordinate two aggregates but never directly mutate their internal state.

---

## 🏗️ Project Structure (Clean Architecture)

```text
com.yeihc
 ├── domain         <-- The Heart: Pure business rules (POJOs).
 │    ├── model     <-- Account (Aggregate), Money (VO), Transfer.
 │    ├── repository <-- Ports (Definitive Interfaces).
 │    └── exception  <-- Business exceptions (Invariant violations).
 ├── application    <-- Orchestration: Use Case logic.
 │    └── usecase   <-- TransferFundsUseCase (Transactional boundaries).
 ├── infrastructure <-- Technical Details: Frameworks & Tools.
 │    ├── persistence <-- JPA Adapters, Spring Data, Entities.
 │    └── config    <-- Spring Beans & Security.
 └── interfaces     <-- Entry Points (Adapters).
      └── rest      <-- Controllers (REST API), DTOs, Mappers.