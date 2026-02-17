package com.yeihc.domain.repository;

import com.yeihc.domain.model.Transfer;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface for the persistence and idempotency control of Transfer operations.
 * * DESIGN DECISIONS:
 * 1. Double-Spending Protection: Includes 'findByIdempotencyKey' to prevent
 * duplicate transactions caused by network retries or client errors.
 * 2. Audit & Recovery: The 'findById' method allows the system to reconstruct
 * the state of a specific transaction for support or reconciliation.
 * 3. Contract for Reliability: Ensures that every transfer lifecycle is
 * traceable and uniquely identified.
 */
public interface TransferRepository {

    /**
     * Persists a Transfer record.
     * Handles both the initial PENDING state and final outcomes (COMPLETED/FAILED).
     * * @param transfer The Transfer aggregate to save.
     */
    void save(Transfer transfer);

    /**
     * Retrieves a transfer by its primary unique identifier.
     * Useful for audit logs, status checks, and transaction history.
     * * @param id The UUID of the transfer.
     * @return An Optional containing the Transfer if found.
     */
    Optional<Transfer> findById(UUID id);

    /**
     * Finds a transfer by a unique idempotency key.
     * This key is typically provided by the client or the API layer to ensure
     * that a specific business intent is processed exactly once.
     * * @param key The unique string key (e.g., a hash of the request or a client-side UUID).
     * @return An Optional containing the previous Transfer attempt, if any.
     */
    Optional<Transfer> findByIdempotencyKey(String key);
}