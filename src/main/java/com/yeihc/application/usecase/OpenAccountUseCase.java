package com.yeihc.application.usecase;

import com.yeihc.application.command.OpenAccountCommand;
import com.yeihc.application.event.DomainEventDispatcher;
import com.yeihc.domain.event.DomainEvent;
import com.yeihc.domain.model.Account;
import com.yeihc.domain.repository.AccountRepository;
import com.yeihc.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

/**
 * Orchestrates the creation of a new bank account aggregate within the system.
 *
 * <p>DESIGN DECISIONS:</p>
 * <ul>
 * <li><b>Aggregate Initialization:</b> Encapsulates the birth of a financial product,
 * ensuring that the initial state (balance and owner) is valid from T0.</li>
 * <li><b>Post-Commit Reliability:</b> Uses the Pull Model for Domain Events to ensure
 * that downstream systems (like notification or KYC services) are only notified
 * if the account is successfully persisted.</li>
 * <li><b>Atomic Onboarding:</b> The entire process is wrapped in a DB transaction
 * to prevent orphaned accounts or inconsistent states.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAccountUseCase {

    private final AccountRepository accountRepository;
    private final DomainEventDispatcher eventDispatcher;

    /**
     * Executes the formal onboarding of a new account.
     *
     * @param command The validated intent containing customer ID and initial deposit.
     * @return The UUID of the newly minted account for tracking and response.
     * @throws DomainException if initial financial invariants are violated.
     */
    @Transactional
    public UUID execute(OpenAccountCommand command) {
        log.info("Processing account opening for customer: {} with initial deposit: {}",
                command.customerId(), command.initialDeposit());

        // 1. Domain Creation
        // The Account constructor acts as a factory, enforcing domain invariants
        // and recording the 'AccountOpenedEvent'.
        Account newAccount = new Account(
                UUID.randomUUID(),
                command.customerId(),
                command.initialDeposit()
        );

        // 2. Persistence
        // Hits the database port. If this fails, the transaction rolls back
        // and no events are dispatched.
        accountRepository.save(newAccount);

        // 3. Event Dispatching (Pull Model)
        // We collect events recorded during the account's initialization.
        List<DomainEvent> events = newAccount.pullDomainEvents();
        dispatchAfterCommit(events);

        log.info("Account {} successfully created for customer {}", newAccount.getId(), command.customerId());
        return newAccount.getId();
    }

    /**
     * Ensures Domain Events are only broadcasted if the transaction successfully commits.
     */
    private void dispatchAfterCommit(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) return;

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventDispatcher.dispatch(events);
                }
            });
        } else {
            // Fallback for non-transactional contexts (e.g., unit or integration tests)
            eventDispatcher.dispatch(events);
        }
    }
}