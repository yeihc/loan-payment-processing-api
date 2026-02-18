package com.yeihc.application.usecase;

import com.yeihc.application.event.DomainEventDispatcher;
import com.yeihc.domain.event.DomainEvent;
import com.yeihc.domain.model.Account;
import com.yeihc.domain.model.Money;
import com.yeihc.domain.model.Transfer;
import com.yeihc.domain.repository.AccountRepository;
import com.yeihc.domain.repository.TransferRepository;
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
 * Orchestrates the fund transfer process between two accounts with integrated auditing.
 * * * DESIGN DECISIONS:
 * 1. Independent Auditing: Uses TransferAuditService to ensure that the creation and
 * failure of transfers are recorded even if the main transaction rolls back.
 * 2. ACID Boundaries: The core financial movement (debit/credit) is kept within a
 * single @Transactional block to maintain strict atomicity.
 * 3. Defensive Flow: Prioritizes the creation of a 'PENDING' record before any
 * balance modification, following the "Log-First" principle.
 */
@Service
@RequiredArgsConstructor
public class TransferFundsUseCase {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final TransferAuditService transferAuditService;
    private final DomainEventDispatcher eventDispatcher;

    /**
     * Executes the transfer operation.
     * * @param sourceId Unique identifier of the origin account.
     * @param targetId Unique identifier of the destination account.
     * @param amount   Monetary value to transfer.
     * @throws DomainException if business rules (status, balance) are violated.
     */
    @Transactional
    public void execute(UUID sourceId, UUID targetId, Money amount, String idempotencyKey) {

        // 1. Validaciones de Orquestación (Fail-fast)
        validateRequest(sourceId, targetId, amount);

        // 2. Control de Idempotencia
        // Si ya existe, simplemente salimos (o lanzamos excepción según política)
        if (transferRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return;
        }

        // 3. Audit Trail con Rollback Protection (REQUIRES_NEW)
        // Se guarda en DB inmediatamente, pase lo que pase después.
        Transfer transfer = transferAuditService.createPending(sourceId, targetId, amount, idempotencyKey);

        try {
            // 4. Carga de Agregados
            Account source = accountRepository.findById(sourceId)
                    .orElseThrow(() -> new DomainException("SOURCE_NOT_FOUND", "Source account not found"));
            Account target = accountRepository.findById(targetId)
                    .orElseThrow(() -> new DomainException("TARGET_NOT_FOUND", "Target account not found"));

            // 5. Lógica de Dominio
            source.debit(amount);
            target.credit(amount);
            transfer.complete();

            // 6. Persistencia (ACID)
            accountRepository.save(source);
            accountRepository.save(target);
            transferRepository.save(transfer);

            // 7. Pull & Dispatch Domain Events
            // Recolectamos eventos de todos los involucrados
            List<DomainEvent> events = new ArrayList<>();
            events.addAll(source.pullDomainEvents());
            events.addAll(target.pullDomainEvents());
            events.addAll(transfer.pullDomainEvents());

            dispatchAfterCommit(events);

        } catch (DomainException e) {
            // 8. Registro de fallo persistente (REQUIRES_NEW)
            transferAuditService.markFailed(transfer.getId(), e.getCode(), e.getMessage());
            throw e; // Rollback de los saldos
        } catch (Exception e) {
            transferAuditService.markFailed(transfer.getId(), "SYSTEM_ERROR", "Unexpected error");
            throw e; // Rollback de los saldos
        }
    }

    private void validateRequest(UUID sourceId, UUID targetId, Money amount) {
        if (sourceId.equals(targetId)) {
            throw new DomainException("SAME_ACCOUNT_TRANSFER", "Source and target accounts must be different");
        }
        if (amount.isLessThanOrEqual(Money.zero())) {
            throw new DomainException("INVALID_AMOUNT", "Transfer amount must be greater than zero");
        }
    }


    private void dispatchAfterCommit(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) return;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventDispatcher.dispatch(events);
            }
        });
    }

}