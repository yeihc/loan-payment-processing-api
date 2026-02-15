📄 ADR 001: Pragmatic Use of JPA Annotations in the Domain
Status
Accepted

Context
The Mini Core Banking project aims to demonstrate a robust design based on Clean Architecture and DDD. However, as a portfolio project with a limited development timeframe, the strict implementation of layer separation (where the Domain is 100% infrastructure-agnostic) requires the creation of:

Domain entity classes.

Persistence entity classes (JPA).

Bidirectional mappers to convert between them.

This "boilerplate" increases complexity and delivery time without adding immediate value to the business logic we want to showcase.

Decision
We have decided to include persistence annotations (JPA/Jakarta) directly in the Domain entities.

Consequences
Coupling: The domain now has a direct dependency on the Jakarta Persistence specification.

Speed: Significant reduction of repetitive code and potential errors in the mapping layer.

Mitigation: * Repository interfaces (Ports) will remain in the Domain, ensuring that application logic does not know how the data is stored.

Business logic (invariants, balance rules) will remain encapsulated in entity methods, separate from persistence properties.

Domain unit tests will run without needing to bring up the persistence context.

Discarded Alternatives
Total Separation (Domain vs. Persistence): Discarded due to the maintenance overhead and the risk of "anemia" in the domain model by trying to keep it too simple to be easily persisted.

Senior Criterion: Responsible Pragmatism
This decision is not due to a lack of understanding of the purity of Clean Architecture, but rather a context-based engineering decision. In professional development, the ability to identify when abstraction becomes a burden is vital. We prefer a rich and functional domain with minimal infrastructure dependency, rather than a "pure" domain that is difficult to evolve due to excessive mappings.