package com.yeihc.domain.event;

import com.yeihc.domain.model.Money;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event emitted when a cross-account money transfer is finalized.
 * * DESIGN DECISIONS:
 * 1. Transactional Correlation: Unlike simple debit/credit events, this event
 * links two distinct accounts, providing a complete picture of the movement.
 * 2. Source & Target Traceability: Includes both account IDs to facilitate
 * the generation of bank statements for both parties from a single event.
 * 3. Atomic Fact: This event signifies that the "Transfer" aggregate has
 * reached its final successful state.
 */
public record TransferCompletedEvent(
        UUID aggregateId,          // Represents the Transfer ID
        UUID sourceAccountId,
        UUID targetAccountId,
        Money amount,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Canonical constructor with multi-entity validation.
     * Ensures all actors and the value of the transfer are present.
     */
    public TransferCompletedEvent {
        if (aggregateId == null) {
            throw new IllegalArgumentException("transferId cannot be null");
        }
        if (sourceAccountId == null) {
            throw new IllegalArgumentException("sourceAccountId cannot be null");
        }
        if (targetAccountId == null) {
            throw new IllegalArgumentException("targetAccountId cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount cannot be null");
        }
        // Guarantees a valid point in time for the audit log
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    /**
     * Convenience constructor for real-time emission.
     * * @param transferId      Unique ID of the transfer process.
     * @param sourceAccountId ID of the account where funds were taken.
     * @param targetAccountId ID of the account where funds were deposited.
     * @param amount          The monetary value moved between accounts.
     */
    public TransferCompletedEvent(UUID transferId, UUID sourceAccountId, UUID targetAccountId, Money amount) {
        this(transferId, sourceAccountId, targetAccountId, amount, Instant.now());
    }
}