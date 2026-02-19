package com.yeihc.domain.repository;

import com.yeihc.domain.model.Transaction;
import java.util.List;
import java.util.UUID;

/**
 * Port (Interface) for persisting and retrieving account transactions.
 * * DESIGN DECISIONS:
 * 1. Append-Only Principle: This repository does not provide 'update' or 'delete'
 * methods. Financial transactions are immutable records of historical facts.
 * 2. Audit Trail Support: Provides methods to reconstruct the history of an
 * account based on its ledger entries.
 * 3. Domain Decoupling: Defines the contract that the Infrastructure layer
 * (JPA/Hibernate) must implement, keeping the domain pure.
 */
public interface TransactionRepository {

    /**
     * Persists a new transaction into the ledger.
     * Since transactions are immutable, this is the most critical operation
     * for financial integrity.
     * * @param transaction The immutable transaction to save.
     */
    void save(Transaction transaction);

    /**
     * Retrieves all ledger entries associated with a specific account.
     * Useful for statement generation and balance reconciliation.
     * * @param accountId The unique identifier of the account.
     * @return A list of transactions ordered by occurrence.
     */
    List<Transaction> findByAccountId(UUID accountId);

    /**
     * Finds a specific transaction by its unique identifier.
     * * @param id The transaction ID.
     * @return The transaction if found, otherwise an empty result.
     */
    java.util.Optional<Transaction> findById(UUID id);
}