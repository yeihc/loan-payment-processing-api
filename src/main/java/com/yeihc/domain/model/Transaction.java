package com.yeihc.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain Entity representing an immutable entry in the account ledger.
 * * DESIGN DECISIONS:
 * 1. Immutability: Once persisted, a transaction MUST NOT be modified or deleted.
 * Corrections must be handled via reversal transactions (Credit/Debit).
 * 2. Decoupling: References the Account via UUID (accountId) to maintain aggregate
 * boundaries and avoid heavy object graphs.
 * 3. Audit Readiness: Captures the precise moment of occurrence and a descriptive
 * reason for the movement.
 */
@Entity
@Table(name = "account_transactions")
@Getter
public class Transaction {

    @Id
    private UUID id;

    /**
     * Link to the account aggregate.
     * We store the ID instead of the object to respect DDD aggregate rules.
     */
    @Column(nullable = false, updatable = false)
    private UUID accountId;

    /**
     * Type of movement: DEBIT (outflow) or CREDIT (inflow).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TransactionType type;

    /**
     * The monetary value of the movement.
     * Uses the Money Value Object to ensure precision.
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "amount", nullable = false, updatable = false))
    private Money amount;

    /**
     * Contextual information (e.g., "Transfer to John Doe", "Initial Deposit").
     */
    @Column(nullable = false, updatable = false)
    private String description;

    /**
     * Timestamp of the transaction creation.
     * Managed by the domain to ensure business accuracy.
     */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Transaction() {
        // Required by JPA
    }

    /**
     * Principal constructor for creating new ledger entries.
     * * @param id          Unique identifier (usually generated via UUID.randomUUID()).
     * @param accountId   The target account for this entry.
     * @param type        DEBIT or CREDIT.
     * @param amount      The Money amount (must be positive).
     * @param description The reason for the transaction.
     */
    public Transaction(UUID id, UUID accountId, TransactionType type, Money amount, String description) {
        this.id = Objects.requireNonNull(id);
        this.accountId = Objects.requireNonNull(accountId);
        this.type = Objects.requireNonNull(type);
        this.amount = Objects.requireNonNull(amount);
        this.description = Objects.requireNonNull(description);
        this.createdAt = Instant.now();

        validate();
    }

    private void validate() {
        if (amount.isLessThanOrEqual(Money.zero())) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }

    /**
     * Movement types for the double-entry accounting logic.
     */
    public enum TransactionType {
        DEBIT, CREDIT
    }
}