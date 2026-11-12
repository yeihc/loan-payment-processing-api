```markdown
# 🏦 Loan Payment Processing API (Bank Transfer Processing Engine)

> A financial processing engine designed under **Hexagonal Architecture** and **Domain-Driven Design (DDD)** principles, prioritizing atomic consistency, idempotency, and absolute traceability.

---

## 📋 Table of Contents
1. [Requirements & Tech Stack](#-requirements--tech-stack)
2. [Critical Architectural Decisions](#-critical-architectural-decisions)
3. [Configuration](#-configuration)
4. [Project Structure](#-project-structure)
5. [Testing Strategy](#-testing-strategy-the-testing-pyramid)
6. [Running the Application](#-running-the-application)
7. [Current Status & Roadmap](#-current-status--roadmap)

---

## 🛠️ Requirements & Tech Stack

* **Java:** `21`
* **Build Tool:** Apache Maven
* **Database:** PostgreSQL (or H2 for local testing/development)
* **Framework:** Spring Boot

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

## ⚙️ Configuration

The application can be configured using standard Spring Boot application profiles (`application.yml` or environment variables).

### Environment Variables
| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | JDBC connection URL for the database | `jdbc:postgresql://localhost:5432/loan_payment_db` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `secret` |
| `SPRING_PROFILES_ACTIVE` | Active profile (`dev`, `prod`, `test`) | `dev` |

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

```

---

## 🧪 Testing Strategy (The Testing Pyramid)

The system features an automated test suite that guarantees the reliability of all fund movements.

### Execution Command

To run all unit and integration tests:

```bash
./mvnw test

```

### Test Breakdown

* **Unit Tests (Domain Layer):**
* `MoneyTest`: Verifies immutable arithmetic, equality by value, and rounding rules.
* `AccountTest`: Validates business invariants (e.g., forbidding closures with remaining balances, negative amounts, or overdrafts).


* **Integration Tests (Application Layer):**
* `TransferFundsUseCaseIT`: Full flow verification ensuring DB persistence, automatic Ledger entry generation (`DEBIT` / `CREDIT`), and exact balance calculations.



---

## 🚀 Running the Application

1. **Clone the repository:**
```bash
git clone [https://github.com/yeihc/loan-payment-processing-api.git](https://github.com/yeihc/loan-payment-processing-api.git)
cd loan-payment-processing-api

```


2. **Configure your Database:** Ensure PostgreSQL is running and update your profile/environment variables if necessary.
3. **Run the application using Maven Wrapper:**
```bash
./mvnw spring-boot:run

```



---

## 📈 Current Status & Roadmap

### ✅ Implemented Features (Current Status)

* User and account registration logic.
* Account deposit, credit, debit, and lifecycle management.
* Atomic fund transfers with idempotency protection.
* Immutable audit trail / ledger generation (`DEBIT` / `CREDIT`).
* Comprehensive unit and integration test suites.

### 🚧 In Development / Roadmap

* **Phase 2:** Implementation of the **Transactional Outbox Pattern** to notify successful transfers asynchronously via message brokers.
* **Phase 3:** Exposing financial health and performance metrics using **Micrometer & Prometheus**.

---

### 💡 Final Note on Design

> *"I did not implement a SAGA pattern because the current system operates within a single ACID database. Introducing distributed complexity would violate the **KISS** principle. However, the design strictly respects Aggregate boundaries, facilitating a future migration to microservices if load requirements demand it."*

```

```