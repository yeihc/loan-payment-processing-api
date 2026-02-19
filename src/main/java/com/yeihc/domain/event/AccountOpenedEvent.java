package com.yeihc.domain.event;

import com.yeihc.domain.model.Money;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event representing the successful creation of a new bank account.
 * * * DESIGN DECISIONS:
 * 1. Immutability via Record: As a fact that happened in the past, this event
 * is inherently immutable and thread-safe.
 * 2. Event-Driven Propagation: Used to trigger side effects such as welcome
 * emails, risk analysis, or ledger initialization without coupling the
 * 'OpenAccount' logic to those services.
 * 3. Pull Model Compatibility: Designed to be stored in the Account's
 * internal event list until the Use Case pulls it for dispatching.
 */
public record AccountOpenedEvent(
        UUID accountId,
        UUID customerId,
        Money initialBalance,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Primary constructor for internal domain use.
     * Automatically captures the current system time as the event occurrence.
     * * @param accountId      The unique identifier of the newly created account.
     * @param customerId     The legal owner of the account.
     * @param initialBalance The starting funds deposited at opening.
     */
    public AccountOpenedEvent(UUID accountId, UUID customerId, Money initialBalance) {
        this(accountId, customerId, initialBalance, Instant.now());
    }

    @Override
    public String type() {
        return "ACCOUNT_OPENED";
    }

    @Override
    public UUID aggregateId() {
        return accountId;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}