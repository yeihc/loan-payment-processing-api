# 📦 Week 1 — Checklist

##  🟢 Create project (Spring Boot)

* Create project in Spring Initializr
* Java 17+
* Spring Boot 3.x
*  Dependencies (Web, JPA, Validation, H2, Lombok)
* Import into the IDE
* Create .gitignore
* Initialize Git
* 
## 2️⃣ Configure build (Maven or Gradle)
* Verify clean pom.xml or build.gradle file
* Define UTF-8 encoding
* Define Java version
* Configure minimum folder structure

## 3️⃣ Define professional package structure

### Create the following packages within src/main/java/...:

* controller
* service
* repository
* domain
* dto
* config
* exception
  
## 4️⃣ Configure application.yml
* Create application.yml
* Configure port (e.g., 8080)
* Configure temporary H2
* Enable H2 console
* Configure spring.jpa.show-sql=false
* Configure ddl-auto=update (development only)

## 5️⃣ Health Base Endpoint (/health)
This endpoint ensures everything is ready to start.
* Create HealthController

* GET /health → return:
* JSON{ "status": "UP" }
  * Test in Postman or browser

* Initial commit:
  * feat: initial project setup with health endpoint

## 🟢 Technical Validation — Everything is working correctly
This application.yml file ensures:
* Server

  * Port 8080 explicitly configured.

* Database

* H2 in-memory → perfect for early development.
Correct driver and URL.

* JPA

  * ddl-auto: update simplifies development.
  * show-sql: false avoids log clutter (very professional).

* H2 Console

  * Access to http://localhost:8080/h2-console.

  * You can use these credentials:

    * Driver: org.h2.Driver
    * JDBC URL: jdbc:h2:mem:testdb
    * Username: sa
    * Password: (empty)

## ✅ 6. Actuator + /actuator/health: a very professional decision
Adding Spring Boot Actuator at this point is a very good sign of sound judgment because:

* ✅ It's an enterprise standard
* ✅ It separates technical health from functional health
* ✅ It brings you closer to the real world (monitoring, SRE, cloud)

In real-world scenarios:

* /health (custom) → can be something functional or business-related
* /actuator/health → the system's technical health

👉 Having Actuator up and running already means that:

* Thinking about operability, not just endpoints
* Aligned with AWS, ECS, ALB, etc.


## ✅ 7. Dev profile in application.yml: sound thinking from the start

# 🏗️ Week 2 — Setup + Domain (Secured DDD + Persistent Transfer + ADR)
🎯 Goal: Make the domain "safe from bad practices": no BigDecimal entering the domain, invariants within the aggregate, traceable and idempotent Transfer, and events ready for "pull".

## 1) Structure and boundaries (Clean Architecture)

Create/confirm layered packages (even if it's in a single module):

* domain (entities, VOs, events, rules)
* application (use cases, commands)
* infrastructure (JPA repos/adapters)
* interfaces (REST, DTOs, mappers)

Rule: Domain does not depend on Application (and ideally not on Infrastructure; see ADR).

## 2) Technical Honesty ADR (JPA in Domain for Pragmatism)

### Create docs/adr/ADR-001-jpa-in-domain.md

Include:

* Context: portfolio + delivery speed
* Decision: use JPA annotations on domain entities
* Consequences: accepted coupling, mitigations (ports, tests, clear boundaries)
* Discarded alternatives: 100% clean mapping with adapters/mapper (more time)

Close with "responsible pragmatism".

## 3) Value Object Money (Total Shielding)
   Senior Rule: The domain will NEVER receive BigDecimal.

Create a Money VO (e.g., amount + scaling/rounding rules)

Explicit prohibition:

* No methods in the domain accept BigDecimal.

* Account.debit(Money amount) / credit(Money amount)

* Transaction.amount is Money

* Validations within Money:

  * Not null
  * Not negative where applicable
  * Scale normalization (e.g., 2 decimal places)

✅ Deliverable: In the domain, any financial transaction only compiles with Money.

## 4) Entities and Aggregates (including persisted Transfers)

* User: Business identity (can exist without accounts)

* Account (Aggregate Root):

  * State: ACTIVE/LOCKED/CLOSED
  * @Version for optimistic locking
  * Invariants within (funds, state)

* Transaction (Immutable Ledger):

  * Immutable (no public setters / no updates)
  * Reversal by offsetting

✅ Transfer (Persisted Entity + Traceability + Idempotency)

* State: PENDING, COMPLETED, FAILED, REVERSED
* IdempotencyKey (unique) for deduplication in the database
* References by ID: sourceAccountId, targetAccountId
* Amount as Money

✅ Deliverable: Transfers exist as entities and can be persisted with their state.

## 5) Domain Events (Pull Model)

* Define the DomainEvent interface/base class

* In aggregates (Account, Transfer), add:

  * private final List<DomainEvent> domainEvents = new ArrayList<>();

  * the pullDomainEvents() method that:

    * returns a copy
    * clears the internal list

* Emit events from the domain:

  * TransferCompletedEvent
  * TransferFailedEvent

  * optional: AccountDebitedEvent, AccountCreditedEvent

## 6) Orchestration Validations (Fail-Fast)

* Source ID != Destination ID
* Quantity > 0 (with isLessThanOrEqual)
* Validations are in Application (not domain)

## 7) Idempotency (Solid Foundation)

* Idempotency Key in Transfer
* Unique Constraint
* findByIdempotencyKey and TransferRepository
* Check early in use case

## 8) Commands:

* OpenAccountCommand
* TransferFundsCommand
*CloseAccountCommand

## 9) Use Cases:

* OpenAccountUseCase ✅ with Command + afterCommit
* TransferFundsUseCase ✅ ACID + idempotent + ledger + afterCommit
* CloseAccountUseCase ✅ idempotent + invariants + afterCommit

*  Conversion to Money outside the domain
* Audit trail with REQUIRES_NEW
* Events dispatched only after commit
  
# Week 3 —🧪 Infrastructure (JPA Adapters)

* JpaUserRepository
* JpaAccountRepository (keyed by @Version)
* JpaTransferRepository (true idempotence by constraint)
* JpaTransactionRepository

# 🚀 Tests

* Unit tests (Money, Account)
* Integration tests (transfer, idempotence)
* Concurrency stress tests (optimistic locking)

# 🌱 Portfolio documentation

README with critical decisions and test results