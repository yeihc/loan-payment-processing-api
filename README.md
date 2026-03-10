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

### 3. True Idempotency (Database Constraint)
Every transfer requires a unique `idempotencyKey`.
* **Reason:** Distributed systems fail. If a client retries a request due to a timeout, the system uses a **Unique Constraint** at the database level to ensure funds move **exactly once**, regardless of how many times the request is received.

### 4. Immutable Ledger (Audit Trail)
Transactions are never edited or deleted. They are **Append-Only** records.
* **Reason:** Compliance with banking audit standards. Any error is corrected with a reversal or adjustment transaction, keeping the historical record intact for reconciliation.
---
#### Account is modeled as the Aggregate Root responsible for protecting balance invariants.
#### Transfers coordinate two aggregates but never directly mutate their internal state.

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
🧪 Testing Strategy (The Testing Pyramid)
The system features an automated test suite that guarantees the reliability of all fund movements.

✅ Unit Tests (Domain Layer)
MoneyTest: Verifies immutable arithmetic, equality by value, and rounding rules.

AccountTest: Validates business invariants (e.g., forbidding closures with remaining balances or overdrafts).

✅ Integration Tests (Application Layer)
TransferFundsUseCaseIT: Full flow verification.

Ensures DB persistence for all entities.

Validates the automatic generation of Ledger entries (Debit/Credit).

Confirms Spring's @Transactional rollback and commit behavior.

🚀 Quick Start Guide
Requirements: Java 17+, Docker (optional for PostgreSQL).

Clone:

Bash
git clone [https://github.com/yeihc/loan-payment-processing-api.git](https://github.com/yeihc/loan-payment-processing-api.git)
Run Tests:

Bash
./mvnw test
Run Application:

Bash
./mvnw spring-boot:run
📈 Roadmap & Evolution
Phase 2: Implementation of the Transactional Outbox Pattern to notify successful transfers via messaging.

Phase 3: Exposing financial health metrics using Micrometer & Prometheus.

💡 Final Note on Design
"I did not implement a SAGA pattern because the current system operates within a single ACID database. Introducing distributed complexity would violate the KISS principle. However, the design strictly respects Aggregate boundaries, facilitating a future migration to microservices if load requirements demand it."


---

### 🚀 Final Polish
This looks incredibly professional. By explicitly mentioning the **Testing Pyramid** and the **Optimistic Locking**, you are speaking the language of senior engineers.

**Would you like me to help you draft the `AccountController` now so you can finally see the API in action with Postman?**