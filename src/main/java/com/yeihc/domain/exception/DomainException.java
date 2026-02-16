package com.yeihc.domain.exception;

/**
 * Base exception for all business rule violations within the Domain layer.
 * * DESIGN DECISIONS:
 * 1. Runtime Exception: Extends RuntimeException to keep domain interfaces clean
 * from 'throws' clauses, following modern Java practices.
 * 2. Error Coding: Includes a specific 'code' (e.g., "INSUFFICIENT_FUNDS") to allow
 * the API layer to map errors to specific HTTP status codes or localized messages.
 * 3. Ubiquitous Language: Used to express business constraints that must not be
 * violated, making the code self-documenting.
 */
public class DomainException extends RuntimeException {

    /**
     * Technical identifier for the business error.
     * Useful for frontend identification and log filtering.
     */
    private final String code;

    /**
     * Primary constructor.
     *
     * @param code    A unique string identifying the error type (e.g., "ACCOUNT_INACTIVE").
     * @param message A descriptive, human-readable explanation of the violation.
     */
    public DomainException(String code, String message) {
        super(message);
        // Integrity check: A domain exception without a code is an anti-pattern.
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("DomainException code cannot be null or blank");
        }
        this.code = code;
    }

    /**
     * @return The specific domain error code.
     */
    public String getCode() {
        return code;
    }
}