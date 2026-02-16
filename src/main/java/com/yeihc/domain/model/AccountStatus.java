package com.yeihc.domain.model;

/**
 * Defines the lifecycle and operational states of a Bank Account.
 * * DESIGN DECISIONS:
 * 1. Explicit Lifecycle: Clearly separates operational states to prevent
 * illegal business actions (e.g., withdrawing from a closed account).
 * 2. Domain Clarity: Uses business-friendly terminology (Ubiquitous Language)
 * instead of technical flags.
 */
public enum AccountStatus {

    /**
     * The account is fully operational.
     * Credits and debits are permitted following standard validation rules.
     */
    ACTIVE,

    /**
     * The account is temporarily restricted.
     * Usually triggered by security protocols, fraud detection, or compliance reviews.
     * State is reversible but prevents any financial movement.
     */
    BLOCKED,

    /**
     * The account is permanently deactivated.
     * This is a terminal state; once closed, an account cannot be reopened
     * or used for further transactions to ensure audit integrity.
     */
    CLOSED
}