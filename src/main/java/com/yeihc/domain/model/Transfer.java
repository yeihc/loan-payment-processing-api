package com.yeihc.domain.model;

import com.yeihc.domain.event.DomainEvent;
import com.yeihc.domain.event.TransferCompletedEvent;
import com.yeihc.domain.event.TransferFailedEvent;
import com.yeihc.domain.exception.DomainException;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing the execution and history of a money transfer.
 * * DESIGN DECISIONS:
 * 1. State Machine: Strictly controls transitions (PENDING -> COMPLETED/FAILED).
 * Once a transfer is finalized, its state becomes immutable.
 * 2. Decoupled Aggregates: References Accounts by UUID instead of direct object
 * mapping to maintain aggregate boundaries and optimize database performance.
 * 3. Auditability: Captures failure metadata (code/reason) to provide
 * observability over banking operations.
 */
@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID sourceAccountId;

    @Column(nullable = false)
    private UUID targetAccountId;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    )
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    private String failureCode;
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(unique = true)
    private String idempotencyKey;

    /**
     * Internal domain event log for the Pull Model.
     */
    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Default constructor required by JPA.
     */
    protected Transfer() {
        // For JPA
    }

    /**
     * Business constructor to initialize a transfer request.
     * * @param id              Unique identifier for this transfer.
     * @param sourceAccountId The origin account ID.
     * @param targetAccountId The destination account ID.
     * @param amount          The money value to be moved.
     */
    public Transfer(UUID id, UUID sourceAccountId, UUID targetAccountId, Money amount, String idempotencyKey) {
        this.id = Objects.requireNonNull(id);
        this.sourceAccountId = Objects.requireNonNull(sourceAccountId);
        this.targetAccountId = Objects.requireNonNull(targetAccountId);
        this.amount = Objects.requireNonNull(amount);
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey); // Requerida para seguridad
        this.status = TransferStatus.PENDING;
        this.createdAt = Instant.now();
    }

    /* =========================
       Domain Behavior
       ========================= */

    /**
     * Transition the transfer to a COMPLETED state.
     * This action is final and triggers the TransferCompletedEvent.
     */
    public void complete() {
        ensurePending();
        this.status = TransferStatus.COMPLETED;
        domainEvents.add(new TransferCompletedEvent(
                id,
                sourceAccountId,
                targetAccountId,
                amount
        ));
    }

    /**
     * Transition the transfer to a FAILED state with diagnostic details.
     * * @param code   Technical error code for system categorization.
     * @param reason Human-readable reason for the failure.
     */
    public void fail(String code, String reason) {
        ensurePending();
        this.status = TransferStatus.FAILED;
        this.failureCode = code;
        this.failureReason = reason;
        domainEvents.add(new TransferFailedEvent(id, code, reason));
    }

    /**
     * Validation guard to ensure state transitions only occur from a PENDING state.
     * Prevents re-processing finalized transfers.
     */
    private void ensurePending() {
        if (status != TransferStatus.PENDING) {
            throw new DomainException(
                    "INVALID_TRANSFER_STATE",
                    "Transfer cannot change state from " + status
            );
        }
    }

    /* =========================
       Domain Events (Pull Model)
       ========================= */

    /**
     * Collects all accumulated events and clears the internal buffer.
     * Used by Application Services to dispatch events to the outside world.
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    /* =========================
       Getters
       ========================= */

    public UUID getId() { return id; }
    public UUID getSourceAccountId() { return sourceAccountId; }
    public UUID getTargetAccountId() { return targetAccountId; }
    public Money getAmount() { return amount; }
    public TransferStatus getStatus() { return status; }
    public String getFailureCode() { return failureCode; }
    public String getFailureReason() { return failureReason; }
}