package com.yeihc.application.command;

import com.yeihc.domain.model.Money;
import java.util.UUID;

/**
 * Command representing the intent to open a new financial account for a customer.
 * * DESIGN DECISIONS:
 * 1. Fail-Fast Validation: Uses a compact constructor to ensure that an invalid
 * command (missing customer or negative deposit) never reaches the Application Service.
 * 2. Immutable Data Carrier: Implemented as a Java Record to guarantee thread-safety
 * and a clear separation from domain entities.
 * 3. Minimalist Intent: Focuses strictly on the data required to initialize an Account
 * aggregate.
 */
public record OpenAccountCommand(
        UUID customerId,
        Money initialDeposit
) {
    /**
     * Compact constructor for data integrity.
     * * @param customerId     The unique identifier of the legal owner.
     * @param initialDeposit The starting balance. Must be zero or positive.
     * @throws IllegalArgumentException if customerId is null or initialDeposit is negative.
     */
    public OpenAccountCommand {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required to bind the account to a legal owner");
        }
        if (initialDeposit == null || initialDeposit.isNegative()) {
            // Business Rule: Accounts cannot be opened with a negative balance.
            throw new IllegalArgumentException("Initial deposit cannot be negative");
        }
    }
}