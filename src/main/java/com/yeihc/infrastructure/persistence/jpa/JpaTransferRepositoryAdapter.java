package com.yeihc.infrastructure.persistence.jpa;

import com.yeihc.domain.model.Transfer;
import com.yeihc.domain.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Secondary Adapter: Implementation of the TransferRepository Port using JPA.
 *
 * <p>This adapter is responsible for the persistent storage of transfer operations.
 * Its primary mission is to ensure <b>Transactional Integrity</b> and <b>Idempotency</b>,
 * acting as the bridge between the domain's transfer logic and the relational database.</p>
 *
 * <p><b>DESIGN DECISIONS:</b></p>
 * <ul>
 * <li><b>Reliable Retries:</b> By implementing 'findByIdempotencyKey', this adapter
 * allows the Application Layer to safely retry failed network requests without
 * duplicating financial movements.</li>
 * <li><b>Persistence Abstraction:</b> It hides the complexity of Spring Data JPA
 * from the Domain, allowing the Transfer aggregate to remain pure and focused on
 * business rules.</li>
 * <li><b>Write-Once Principle:</b> In this context, transfers are treated as
 * immutable events. This adapter only supports creation and retrieval,
 * matching the non-destructive nature of financial ledgers.</li>
 * </ul>
 */
@Repository
@RequiredArgsConstructor
public class JpaTransferRepositoryAdapter implements TransferRepository {

    private final SpringDataTransferRepository springDataRepository;

    /**
     * Persists a new transfer record.
     * <p>Note: If the underlying table has a Unique Constraint on 'idempotency_key',
     * this method will throw a DataIntegrityViolationException on duplicate attempts,
     * providing a final layer of safety.</p>
     * * @param transfer The transfer aggregate to save.
     */
    @Override
    public void save(Transfer transfer) {
        springDataRepository.save(transfer);
    }

    /**
     * Retrieves a transfer by its technical primary key (UUID).
     * Useful for audit logs and administrative lookups.
     */
    @Override
    public Optional<Transfer> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    /**
     * Look up a transfer by its business-provided idempotency key.
     * This is the cornerstone of our duplicate prevention strategy.
     * * @param key The unique key provided by the client/caller.
     * @return Optional containing the existing transfer if it was previously processed.
     */
    @Override
    public Optional<Transfer> findByIdempotencyKey(String key) {
        return springDataRepository.findByIdempotencyKey(key);
    }
}