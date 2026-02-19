package com.yeihc.application.usecase;

import com.yeihc.application.command.TransferFundsCommand;
import com.yeihc.application.event.DomainEventDispatcher;
import com.yeihc.domain.event.DomainEvent;
import com.yeihc.domain.model.Account;
import com.yeihc.domain.model.Money;
import com.yeihc.domain.model.Transfer;
import com.yeihc.domain.model.Transaction;
import com.yeihc.domain.repository.AccountRepository;
import com.yeihc.domain.repository.TransferRepository;
import com.yeihc.domain.repository.TransactionRepository;
import com.yeihc.domain.exception.DomainException;
import com.yeihc.application.service.TransferAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrator for the Fund Transfer Use Case.
 * * DESIGN DECISIONS:
 * 1. Log-First Resilience: Creates a 'PENDING' audit record before modifying any
 * account balance, ensuring traceability even during system crashes.
 * 2. Inmutable Ledger: For every balance change, it persists a Transaction entry
 * (debit/credit) into the account_transactions table for historical auditing.
 * 3. Idempotency Guard: Prevents duplicate processing by checking the
 * unique 'idempotencyKey' provided by the client.
 * 4. Post-Commit Dispatching: Uses TransactionSynchronization to ensure Domain Events
 * are only dispatched if the database transaction commits successfully.
 */
@Service
@RequiredArgsConstructor
public class TransferFundsUseCase {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final TransactionRepository transactionRepository;
    private final TransferAuditService transferAuditService;
    private final DomainEventDispatcher eventDispatcher;

    /**
     * Executes the business logic for moving funds between two accounts.
     * * @param command Data transfer object containing IDs, amount, and idempotency key.
     * @throws DomainException if funds are insufficient, accounts are inactive,
     * or IDs are invalid.
     */
    @Transactional
    public void execute(TransferFundsCommand command) {
        // 1. Orchestration Validations
        validateRequest(command.sourceAccountId(), command.targetAccountId());

        // 2. Idempotency Check
        if (transferRepository.findByIdempotencyKey(command.idempotencyKey()).isPresent()) {
            return;
        }

        // 3. Resilient Audit Trail (REQUIRES_NEW)
        Transfer transfer = transferAuditService.createPending(
                command.sourceAccountId(),
                command.targetAccountId(),
                command.amount(),
                command.idempotencyKey()
        );

        try {
            // 4. Load Aggregates
            Account source = accountRepository.findById(command.sourceAccountId())
                    .orElseThrow(() -> new DomainException("SOURCE_NOT_FOUND", "Source account not found"));
            Account target = accountRepository.findById(command.targetAccountId())
                    .orElseThrow(() -> new DomainException("TARGET_NOT_FOUND", "Target account not found"));

            // 5. Domain Logic & Ledger Generation
            // The domain entities handle balance changes and return immutable ledger entries
            Transaction debitEntry = source.debit(command.amount(), "Transfer to account " + command.targetAccountId());
            Transaction creditEntry = target.credit(command.amount(), "Transfer from account " + command.sourceAccountId());
            transfer.complete();

            // 6. ACID Persistence
            accountRepository.save(source);
            accountRepository.save(target);
            transferRepository.save(transfer);

            // Persist the immutable audit trail (Ledger)
            transactionRepository.save(debitEntry);
            transactionRepository.save(creditEntry);

            // 7. Pull & Dispatch Domain Events
            List<DomainEvent> events = new ArrayList<>();
            events.addAll(source.pullDomainEvents());
            events.addAll(target.pullDomainEvents());
            events.addAll(transfer.pullDomainEvents());

            dispatchAfterCommit(events);

        } catch (DomainException e) {
            // Persist failure reason using an independent transaction
            transferAuditService.markFailed(transfer.getId(), e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            transferAuditService.markFailed(transfer.getId(), "SYSTEM_ERROR", "Unexpected error: " + e.getMessage());
            throw e;
        }
    }

    private void validateRequest(UUID sourceId, UUID targetId) {
        if (sourceId.equals(targetId)) {
            throw new DomainException("SAME_ACCOUNT_TRANSFER", "Source and target accounts must be different");
        }
    }

    /**
     * Ensures events are dispatched to external listeners ONLY after the
     * DB transaction has been successfully finalized.
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
            eventDispatcher.dispatch(events);
        }
    }
}