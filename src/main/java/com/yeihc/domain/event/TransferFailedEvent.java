package com.yeihc.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event emitted when a transfer process cannot be completed.
 * * DESIGN DECISIONS:
 * 1. Diagnostic Focus: Unlike successful events, this one prioritizes failure
 * metadata (code and message) to facilitate troubleshooting and automated recovery.
 * 2. Non-Blocking Resilience: Emitting this event allows the system to trigger
 * compensating transactions or user notifications without keeping the thread busy.
 * 3. Audit Readiness: Captures the 'transferId' (aggregateId) to correlate
 * the failure with the original transfer request in log aggregation tools.
 */
public record TransferFailedEvent(
        UUID aggregateId,          // Represents the Transfer ID
        String failureCode,        // Technical error code (e.g., INSUFFICIENT_FUNDS)
        String failureMessage,     // Human-readable description
        Instant occurredAt
) implements DomainEvent {

    /**
     * Canonical constructor with mandatory validation.
     * Guarantees that no "silent failures" occur by requiring diagnostic data.
     */
    public TransferFailedEvent {
        if (aggregateId == null) {
            throw new IllegalArgumentException("transferId cannot be null");
        }
        if (failureCode == null || failureCode.isBlank()) {
            throw new IllegalArgumentException("failureCode cannot be blank");
        }
        if (failureMessage == null || failureMessage.isBlank()) {
            throw new IllegalArgumentException("failureMessage cannot be blank");
        }
        // Ensure chronological accuracy for the failure log
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    /**
     * Convenience constructor for immediate failure reporting.
     * * @param transferId     The unique identifier of the attempted transfer.
     * @param failureCode    A standardized string code representing the error type.
     * @param failureMessage A descriptive message for logging or UI display.
     */
    public TransferFailedEvent(UUID transferId, String failureCode, String failureMessage) {
        this(transferId, failureCode, failureMessage, Instant.now());
    }
}