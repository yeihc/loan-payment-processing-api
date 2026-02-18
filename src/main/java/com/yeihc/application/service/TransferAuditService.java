package com.yeihc.application.service;

import com.yeihc.domain.model.Money;
import com.yeihc.domain.model.Transfer;
import com.yeihc.domain.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service dedicated to the persistent auditing of transfer attempts.
 * * DESIGN DECISIONS:
 * 1. Transactional Independence: Uses REQUIRES_NEW to suspend the current transaction
 * and create a new one. This ensures audit logs are saved even if the main
 * financial transaction rolls back.
 * 2. Integrity: Acts as a safeguard to ensure that the lifecycle of a transfer
 * is always traceable, from the initial intent to the final outcome.
 */
@Service
@RequiredArgsConstructor
public class TransferAuditService {

    private final TransferRepository transferRepository;

    /**
     * Creates and persists an initial transfer record in a separate transaction.
     * This guarantees that we have a record of the intent before the balance
     * modification starts.
     *
     * @return The created Transfer aggregate in PENDING state.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transfer createPending(UUID source, UUID target, Money amount, String idempotencyKey) {
        Transfer t = new Transfer(UUID.randomUUID(), source, target, amount, idempotencyKey);
        transferRepository.save(t);
        return t;
    }

    /**
     * Updates a transfer record to FAILED status in a separate transaction.
     * Called when a DomainException or System Error occurs in the main flow.
     *
     * @param transferId The ID of the transfer to update.
     * @param code       The error code for categorization.
     * @param reason     Human-readable explanation of the failure.
     * @throws IllegalStateException if the transfer record was not properly initialized.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID transferId, String code, String reason) {
        // We use IllegalStateException because the absence of a transfer record
        // at this stage indicates a fundamental flow failure, not a business error.
        Transfer t = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalStateException("Critical Error: Transfer record [" + transferId + "] not found for auditing."));

        t.fail(code, reason);
        transferRepository.save(t);
    }
}