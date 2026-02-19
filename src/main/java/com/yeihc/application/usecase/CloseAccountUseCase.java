package com.yeihc.application.usecase;

import com.yeihc.application.command.CloseAccountCommand;
import com.yeihc.application.event.DomainEventDispatcher;
import com.yeihc.domain.exception.DomainException;
import com.yeihc.domain.model.Account;
import com.yeihc.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Orchestrator for the "Close Account" business process.
 *
 * <p>DESIGN DECISIONS:</p>
 * <ul>
 * <li><b>Financial Integrity (Zero-Balance):</b> Strict enforcement of the invariant that an account
 * must have exactly zero balance before termination to prevent loss of funds or debt evasion.</li>
 * <li><b>Idempotency:</b> Designed to be safe for retries. If the account is already in a
 * CLOSED state, the process terminates gracefully without error.</li>
 * <li><b>Reliable Notification:</b> Employs Transaction Synchronization to ensure that domain events
 * (like 'AccountClosedEvent') are only dispatched to external systems if the database
 * successfully commits the state change.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloseAccountUseCase {

    private final AccountRepository accountRepository;
    private final DomainEventDispatcher eventDispatcher;

    /**
     * Executes the formal closure of a bank account.
     *
     * @param command The closure request containing the account identifier and justification.
     * @throws DomainException if the account is not found or fails the zero-balance business rule.
     */
    @Transactional
    public void execute(CloseAccountCommand command) {
        log.info("Attempting to close account: {} due to: {}", command.accountId(), command.reason());

        // 1. Load Aggregate
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new DomainException("ACCOUNT_NOT_FOUND", "No account exists with the provided ID"));

        // 2. Idempotency Check
        // If the goal is already achieved, we return early to avoid unnecessary processing.
        if (account.isClosed()) {
            log.warn("Account {} is already closed. Skipping operation.", command.accountId());
            return;
        }

        // 3. Business Rule Validation: Zero-Balance Invariant
        // This is a critical safety check before invoking domain logic.
        if (!account.getBalance().isZero()) {
            throw new DomainException("ACCOUNT_NOT_EMPTY",
                    String.format("Account has a remaining balance of %s. Must be zero to close.", account.getBalance()));
        }

        // 4. Domain Logic Execution
        // The Account aggregate handles its state transition and records the 'AccountClosedEvent'.
        account.close(command.reason());

        // 5. Persistence
        accountRepository.save(account);

        // 6. Post-Commit Event Dispatching
        // Ensures side effects (emails, card cancellations) only happen if the DB is updated.
        dispatchAfterCommit(account);

        log.info("Account {} successfully closed.", command.accountId());
    }

    /**
     * Registers a synchronization task to dispatch domain events after a successful DB commit.
     */
    private void dispatchAfterCommit(Account account) {
        var events = account.pullDomainEvents();
        if (events.isEmpty()) return;

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventDispatcher.dispatch(events);
                }
            });
        } else {
            // Fallback for non-transactional contexts (e.g., unit tests)
            eventDispatcher.dispatch(events);
        }
    }
}