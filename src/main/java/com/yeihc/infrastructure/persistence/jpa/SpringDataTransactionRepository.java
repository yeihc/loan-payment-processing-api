package com.yeihc.infrastructure.persistence.jpa;

import com.yeihc.domain.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Infrastructure Layer: Spring Data JPA Repository for Transaction.
 * * <p>Focuses on retrieving the immutable financial history of accounts.
 * Queries are optimized to return results in reverse chronological order
 * for better audit visibility.</p>
 */
@Repository
public interface SpringDataTransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Retrieves all transactions associated with an account, sorted by newest first.
     * @param accountId The ID of the account to audit.
     * @return A list of historical transactions.
     */
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}