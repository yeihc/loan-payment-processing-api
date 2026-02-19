package com.yeihc.application.command;

import java.util.UUID;

/**
 * Command representing the formal intent to terminate a bank account's lifecycle.
 * * DESIGN DECISIONS:
 * 1. Definitive Action: This command triggers the transition of an Account aggregate
 * to a terminal state (CLOSED).
 * 2. Mandatory Justification: Requires a 'reason' to satisfy compliance and
 * regulatory auditing requirements (KYC/Retention analysis).
 * 3. Immutable Record: As a Command, it ensures that once the request is sent to
 * the Application layer, its data cannot be altered during execution.
 */
public record CloseAccountCommand(
        UUID accountId,
        String reason
) {
    /**
     * Compact constructor for proactive data validation (Fail-Fast).
     * * @param accountId The unique identifier of the account to be closed.
     * @param reason    The justification for the closure (e.g., "Customer request",
     * "Policy violation", "Account inactivity").
     * @throws IllegalArgumentException if accountId is null or reason is empty/blank.
     */
    public CloseAccountCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID is mandatory for closure operations");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A valid reason is required for regulatory auditing purposes");
        }
    }
}