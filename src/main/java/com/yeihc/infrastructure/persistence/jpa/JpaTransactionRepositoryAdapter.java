package com.yeihc.infrastructure.persistence.jpa;

import com.yeihc.domain.model.Transaction;
import com.yeihc.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary Adapter: Manages the persistence of the financial Ledger.
 *
 * <p><b>DESIGN DECISIONS:</b></p>
 * <ul>
 * <li><b>Immutable History:</b> This adapter intentionally lacks 'update' or 'delete'
 * methods, enforcing the business rule that financial records cannot be modified.</li>
 * <li><b>Domain Decoupling:</b> Implements 'TransactionRepository' to keep the
 * domain layer unaware of JPA or Spring Data.</li>
 * </ul>
 */
@Repository
@RequiredArgsConstructor
public class JpaTransactionRepositoryAdapter implements TransactionRepository {

    private final SpringDataTransactionRepository springDataRepository;

    /**
     * Persists a transaction as a permanent historical record.
     */
    @Override
    public void save(Transaction transaction) {
        springDataRepository.save(transaction);
    }

    /**
     * Retrieves the complete transaction history for a specific account.
     */
    @Override
    public List<Transaction> findByAccountId(UUID accountId) {
        return springDataRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    /**
     * Finds a single transaction by its unique ID for auditing or reconciliation.
     */
    @Override
    public Optional<Transaction> findById(UUID id) {
        return springDataRepository.findById(id);
    }
}