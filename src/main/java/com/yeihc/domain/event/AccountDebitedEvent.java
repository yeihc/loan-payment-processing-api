package com.yeihc.domain.event;

import com.yeihc.domain.model.Money;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event emitted when funds are successfully debited (withdrawn) from an account.
 * * DESIGN DECISIONS:
 * 1. Historical Fact: Represents a successful state change. In financial systems,
 * once a debit is confirmed, it is recorded as an immutable event for audit trails.
 * 2. Payload Integrity: Encapsulates the specific amount and the affected aggregate
 * to allow reconstruction of account state if needed (Event Sourcing foundation).
 * 3. Timing: Uses Instant (UTC) to ensure chronological ordering, which is
 * critical for preventing overdrafts in distributed environments.
 */
public record AccountDebitedEvent(
        UUID aggregateId,
        Money amount,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Canonical constructor with validation logic.
     * Ensures that every debit event recorded has a valid source and amount.
     */
    public AccountDebitedEvent {
        if (aggregateId == null) {
            throw new IllegalArgumentException("aggregateId cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount cannot be null");
        }
        // Fallback to current time if not provided, ensuring event timeline continuity
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    /**
     * Secondary constructor for immediate event creation.
     * * @param accountId The UUID of the account that was debited.
     * @param amount    The value subtracted from the balance.
     */
    public AccountDebitedEvent(UUID accountId, Money amount) {
        this(accountId, amount, Instant.now());
    }
}