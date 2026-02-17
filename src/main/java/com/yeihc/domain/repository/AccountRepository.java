package com.yeihc.domain.repository;

import com.yeihc.domain.model.Account;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface for managing the persistence of Account aggregates.
 * * DESIGN DECISIONS:
 * 1. Interface in Domain: Following Dependency Inversion, the Domain layer defines
 * the contract, and the Infrastructure layer (JPA) implements it.
 * 2. Aggregate Root: This repository only deals with the Account aggregate root,
 * ensuring that all consistency boundaries are respected.
 * 3. Minimalist Contract: Only exposes methods strictly required by use cases,
 * adhering to the Interface Segregation Principle.
 */
public interface AccountRepository {

    /**
     * Retrieves an account by its unique identifier.
     * * @param id The UUID of the account to find.
     * @return An Optional containing the Account if found, or empty if it does not exist.
     */
    Optional<Account> findById(UUID id);

    /**
     * Persists the current state of the Account aggregate.
     * In the implementation, this should handle both creation and updates,
     * typically involving the synchronization of the @Version field for optimistic locking.
     * * @param account The aggregate instance to be saved.
     */
    void save(Account account);
}