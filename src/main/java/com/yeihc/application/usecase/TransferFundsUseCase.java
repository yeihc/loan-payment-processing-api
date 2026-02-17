package com.yeihc.application.usecase;

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

    /**
     * Executes the transfer operation.
     * * @param sourceId Unique identifier of the origin account.
     * @param targetId Unique identifier of the destination account.
     * @param amount   Monetary value to transfer.
     * @throws DomainException if business rules (status, balance) are violated.
     */
    @Transactional
    public void execute(UUID sourceId, UUID targetId, Money amount) {

        // 1. Pre-Transaction Audit: Create a persistent record of the intent.
        // Assuming createPending uses Propagation.REQUIRES_NEW to commit immediately.
        Transfer transfer = transferAuditService.createPending(sourceId, targetId, amount);

        try {
            // 2. Aggregate Loading
            Account source = accountRepository.findById(sourceId)
                    .orElseThrow(() -> new DomainException("SOURCE_NOT_FOUND", "Source account not found"));

            Account target = accountRepository.findById(targetId)
                    .orElseThrow(() -> new DomainException("TARGET_NOT_FOUND", "Target account not found"));

            // 3. Domain Logic Execution
            // Invariants are checked inside these methods (insufficient funds, active status).
            source.debit(amount);
            target.credit(amount);

            // 4. Update the local Transfer state to success
            transfer.complete();

            // 5. Finalize Main Transaction
            // This saves both accounts and the transfer's COMPLETED status in one atomic commit.
            accountRepository.save(source);
            accountRepository.save(target);
            transferRepository.save(transfer);

            // TODO: Dispatch Domain Events here (pullDomainEvents)

        } catch (DomainException e) {
            // 6. Controlled Business Failure
            // Explicitly record the reason for failure in a separate transaction.
            transferAuditService.markFailed(transfer.getId(), e.getCode(), e.getMessage());
            throw e; // Rethrow to notify the caller and rollback balance changes if any.

        } catch (Exception e) {
            // 7. Uncontrolled Technical Failure
            transferAuditService.markFailed(transfer.getId(), "SYSTEM_ERROR", "Unexpected error");
            throw e;
        }
    }
}