package com.yeihc.infrastructure.persistence.jpa;

import com.yeihc.domain.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure Adapter: Spring Data JPA implementation for the Account entity.
 *
 * <p>This interface acts as the physical persistence layer for bank accounts.
 * By extending JpaRepository, it provides standard CRUD operations and
 * leverages Hibernate's Optimistic Locking mechanism via the '@Version' field
 * defined in the domain aggregate.</p>
 *
 * <p><b>DESIGN DECISIONS:</b></p>
 * <ul>
 * <li><b>Derived Queries:</b> Uses Spring Data naming conventions to generate
 * optimized SQL queries for business-specific identifiers like 'accountNumber'.</li>
 * <li><b>Concurrency Control:</b> Implicitly handles version checking to protect
 * account balances from race conditions during high-volume transactions.</li>
 * <li><b>Performance:</b> Optimized 'exists' checks are provided to avoid
 * loading full entities when only presence verification is required.</li>
 * </ul>
 */
@Repository
public interface SpringDataAccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Retrieves an account based on its unique business identifier.
     * Unlike the UUID, the Account Number is the identifier used by customers.
     *
     * @param accountNumber The formatted account string (e.g., "ACC-123456").
     * @return An Optional containing the Account if it exists in the database.
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Verifies if an account number is already registered.
     * Essential for the Account Number generation strategy to prevent collisions.
     *
     * @param accountNumber The number to verify for existence.
     * @return true if the account number is already present in the system.
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * [Optional/Future] Retrieves all accounts associated with a specific owner.
     * Useful for dashboard and customer profile views.
     * * List<Account> findByCustomerId(UUID customerId);
     */
}