package com.yeihc.domain.model;

import com.yeihc.domain.event.AccountCreditedEvent;
import com.yeihc.domain.event.AccountDebitedEvent;
import com.yeihc.domain.event.DomainEvent;
import com.yeihc.domain.exception.DomainException;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root representing a Bank Account.
 * * DESIGN DECISIONS:
 * 1. Rich Domain Model: The entity protects its own invariants (balance cannot be negative,
 * account must be active for transactions).
 * 2. Event Sourcing Lite (Pull Model): State changes trigger domain events which are
 * collected internally and cleared upon dispatch.
 * 3. Concurrency Control: Uses JPA @Version (Optimistic Locking) to prevent race conditions
 * during simultaneous balance updates.
 * 4. Pragmatic Persistence: Directly uses JPA annotations to simplify deployment (ADR-001).
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Embedded
    private Money balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    /**
     * Optimistic locking version.
     * Essential for financial systems to prevent "lost updates"
     * when two transactions attempt to modify the balance simultaneously.
     */
    @Version
    private Long version;

    /**
     * Temporal storage for domain events triggered by state changes.
     * Marked as @Transient because events are not persisted with the entity state.
     */
    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // --- Constructors ---

    /**
     * Required by JPA/Hibernate. Protected to discourage direct instantiation
     * outside the domain context.
     */
    protected Account() {
        // Required for JPA
    }

    /**
     * Business constructor for new accounts.
     * Defaults balance to zero and status to ACTIVE.
     */
    public Account(UUID id, String accountNumber) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = Money.zero();
        this.status = AccountStatus.ACTIVE;
    }

    // --- Domain Behavior (Business Logic) ---

    /**
     * Decreases the account balance.
     * @throws DomainException if account is inactive or has insufficient funds.
     */
    public void debit(Money amount) {
        validateActive();

        if (balance.isLessThan(amount)) {
            throw new DomainException(
                    "INSUFFICIENT_FUNDS",
                    "The current balance " + balance + " is not enough for the requested debit of " + amount
            );
        }

        this.balance = this.balance.subtract(amount);
        domainEvents.add(new AccountDebitedEvent(this.id, amount));
    }

    /**
     * Increases the account balance.
     * @throws DomainException if account is inactive.
     */
    public void credit(Money amount) {
        validateActive();

        this.balance = this.balance.add(amount);
        domainEvents.add(new AccountCreditedEvent(this.id, amount));
    }

    /**
     * Ensures the account is in a valid state for financial movements.
     */
    private void validateActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new DomainException(
                    "ACCOUNT_NOT_ACTIVE",
                    "Account " + accountNumber + " is currently in " + status + " status."
            );
        }
    }

    // --- Domain Events Management (Pull Model) ---

    /**
     * Captures and clears accumulated domain events.
     * To be called by the Application Service after persisting the entity.
     * @return A copy of the domain events list.
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    // --- Getters (Business Accessors) ---

    public UUID getId() {
        return id;
    }

    public Money getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }
}