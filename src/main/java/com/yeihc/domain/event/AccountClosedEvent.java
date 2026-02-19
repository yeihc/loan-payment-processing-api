package com.yeihc.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event representing the definitive closure of a bank account.
 * * <p>DESIGN DECISIONS:</p>
 * <ul>
 * <li><b>Historical Fact:</b> As a record of something that has already occurred,
 * this event is immutable and carries the context of the closure.</li>
 * <li><b>Reactive Integration:</b> Designed to be consumed by infrastructure listeners
 * to trigger side effects such as revoking API keys, canceling physical cards,
 * or updating CRM status.</li>
 * <li><b>Traceability:</b> Captures the 'reason' provided during the process,
 * which is essential for anti-fraud and compliance audits.</li>
 * </ul>
 */
public record AccountClosedEvent(
        UUID accountId,
        String reason,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Primary constructor for domain use.
     * * @param accountId The unique identifier of the account that was closed.
     * @param reason    The justification for the closure (e.g., "Customer Request", "Regulatory Breach").
     */
    public AccountClosedEvent(UUID accountId, String reason) {
        this(accountId, reason, Instant.now());
    }

    @Override
    public String type() {
        return "ACCOUNT_CLOSED";
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