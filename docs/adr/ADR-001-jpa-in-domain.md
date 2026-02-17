# Mini Core Banking – Architectural Decision Record (ADR)

## Context
The **Mini Core Banking** project aims to demonstrate a solid backend design grounded in **Domain-Driven Design (DDD)** and **Clean Architecture**, with a strong focus on:
- Domain modeling
- Consistency
- Business invariants

In a strict interpretation of Clean Architecture, the Domain layer should be completely infrastructure‑agnostic. This would require:
- Separate Domain entities (pure business objects)
- Separate Persistence entities (JPA‑mapped)
- Bidirectional mappers between both models

While architecturally “pure,” this approach introduces significant boilerplate, cognitive overhead, and delivery cost. For a portfolio project whose goal is to showcase business modeling and technical judgment, this extra complexity does not provide proportional value.

---

## Decision
We decided to apply **Jakarta Persistence (JPA)** annotations directly on Domain entities.  
This means that entities such as `Account` and `Transfer` are simultaneously:
- Rich domain models (holding invariants and behavior)
- Persistence‑mapped entities

👉 This decision prioritizes **clarity, delivery speed, and domain correctness** over absolute architectural purity.

---

## Consequences

### Coupling
The Domain layer becomes directly dependent on the Jakarta Persistence specification.

### Benefits
- Significant reduction of boilerplate code
- No duplication of domain concepts across layers
- Faster iteration and clearer domain modeling

### Mitigations
To control the impact of this coupling:
- Repository interfaces (Ports) remain defined in the Domain layer
- Application logic depends on abstractions, not persistence details
- Business rules and invariants remain encapsulated within entity methods
- Domain unit tests can be executed without starting a persistence context

---

## Discarded Alternatives
**Full Separation between Domain and Persistence Models**  
This approach was discarded due to:
- High maintenance overhead
- Risk of anemic domain models
- Increased mapping complexity with little benefit at this stage

---

## Senior Criterion: Responsible Pragmatism
This decision does not stem from a lack of understanding of Clean Architecture principles. On the contrary, it reflects an **intentional, context‑aware engineering choice**.

In professional software development, recognizing when abstraction becomes a liability is as important as knowing how to apply it. For this project, we favor a **rich, expressive, and maintainable domain** with minimal infrastructure leakage over a perfectly isolated but harder‑to‑evolve model.
