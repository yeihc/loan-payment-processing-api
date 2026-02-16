package com.yeihc.domain.event;

import com.yeihc.domain.model.Money;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event emitted when funds are successfully credited to an account.
 * * DESIGN DECISIONS:
 * 1. Immutability: Uses a Java Record to ensure the event payload cannot be altered
 * after creation, representing an immutable fact in the past.
 * 2. Traceability: Includes 'occurredAt' to provide a reliable timeline for
 * auditing and eventual consistency synchronization.
 * 3. Identity: Uses 'aggregateId' (the Account UUID) to follow DDD patterns,
 * allowing subscribers to identify which entity triggered the change.
 */
public record AccountCreditedEvent(
        UUID aggregateId,
        Money amount,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Canonical constructor with business integrity guards.
     */
    public AccountCreditedEvent {
        if (aggregateId == null) {
            throw new IllegalArgumentException("aggregateId cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount cannot be null");
        }
        // Ensures occurredAt is always populated even if not provided by the caller
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    /**
     * Convenience factory constructor.
     * Simplifies event creation by automatically capturing the current system timestamp.
     *
     * @param accountId Unique identifier of the credited account.
     * @param amount    The monetary value added to the balance.
     */
    public AccountCreditedEvent(UUID accountId, Money amount) {
        this(accountId, amount, Instant.now());
    }
}