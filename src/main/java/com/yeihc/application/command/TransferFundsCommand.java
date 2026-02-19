package com.yeihc.application.command;

import com.yeihc.domain.model.Money;
import java.util.UUID;

/**
 * Command representing the intent to execute a fund transfer.
 * * DESIGN DECISIONS:
 * 1. Data Integrity: Uses a compact constructor to enforce invariants before the
 * Command is even instantiated.
 * 2. Idempotency Support: Carrying the 'idempotencyKey' from the entry point ensures
 * that retries are handled safely across the entire execution chain.
 * 3. Immutable Record: Being a record, it is thread-safe and purely carries data
 * from the boundary into the domain.
 */
public record TransferFundsCommand(
        UUID sourceAccountId,
        UUID targetAccountId,
        Money amount,
        String idempotencyKey
) {
    /**
     * Compact constructor for early validation.
     * * @throws IllegalArgumentException if mandatory identifiers are missing,
     * the amount is invalid, or the idempotency key is blank.
     */
    public TransferFundsCommand {
        if (sourceAccountId == null || targetAccountId == null) {
            throw new IllegalArgumentException("Account IDs are mandatory to define source and target");
        }
        if (amount == null || amount.isLessThanOrEqual(Money.zero())) {
            throw new IllegalArgumentException("Transfer amount must be a positive value greater than zero");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("An idempotency key is required to prevent duplicate processing");
        }
    }
}