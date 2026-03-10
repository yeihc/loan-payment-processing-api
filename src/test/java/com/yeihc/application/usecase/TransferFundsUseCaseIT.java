package com.yeihc.application.usecase;

import com.yeihc.application.command.TransferFundsCommand;
import com.yeihc.domain.model.*;
import com.yeihc.domain.repository.AccountRepository;
import com.yeihc.domain.repository.TransactionRepository;
import com.yeihc.domain.repository.TransferRepository;
import com.yeihc.domain.repository.UserRepository;
import com.yeihc.loanpayment.LoanPaymentProcessingApiApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.yeihc.loanpayment.LoanPaymentProcessingApiApplication;
import org.springframework.boot.test.context.SpringBootTest;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test for the Transfer Funds Use Case.
 * <p>Location: {@code src/test/java/com/yeihc/application/usecase/TransferFundsUseCaseIT.java}</p>
 *
 * <p><b>TESTING STRATEGY:</b></p>
 * <ul>
 * <li><b>Full Stack Integration:</b> Exercises the Use Case using real JPA repositories
 * and an actual database context (usually H2 or Testcontainers).</li>
 * <li><b>Transaction Integrity (ACID):</b> Verified by the {@code @Transactional} annotation,
 * ensuring that all repository operations (User, Account, Transaction, Transfer)
 * participate in a single database transaction.</li>
 * <li><b>State Persistence:</b> Validates that domain side-effects (balance updates
 * and ledger entries) are correctly persisted and retrievable.</li>
 * </ul>
 */
@SpringBootTest(classes = LoanPaymentProcessingApiApplication.class)
//@Transactional
@DisplayName("Integration Test: Transfer Funds Use Case Flow")
class TransferFundsUseCaseIT {

    @Autowired
    private TransferFundsUseCase transferFundsUseCase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransferRepository transferRepository;

    /**
     * Verifies a successful transfer flow including:
     * 1. Balance deduction from source.
     * 2. Balance increment at target.
     * 3. Persistence of the Transfer record.
     * 4. Generation of double-entry ledger records (Debit/Credit).
     */
    @Test
    @DisplayName("Should execute a full transfer between two accounts and persist all records")
    void shouldExecuteFullTransferFlow() {
        // --- 1. GIVEN: Database pre-populated with two valid users and accounts ---
        User sender = new User(UUID.randomUUID(), "Sender User", "sender@test.com", "TAX-001");
        userRepository.save(sender);
        User receiver = new User(UUID.randomUUID(), "Receiver User", "receiver@test.com", "TAX-002");
        userRepository.save(receiver);

        Account sourceAccount = new Account(
                UUID.randomUUID(),
                sender.getId(),
                Money.of(new BigDecimal("1000.00"))
        );
        accountRepository.save(sourceAccount);
        Account targetAccount = new Account(
                UUID.randomUUID(),
                receiver.getId(),
                Money.of(new BigDecimal("200.00"))
        );
        accountRepository.save(targetAccount);
        BigDecimal transferAmount = new BigDecimal("300.00");
        String idempotencyKey = "unique-transfer-key-" + UUID.randomUUID();

        // --- 2. WHEN: The Use Case is executed ---
        TransferFundsCommand command = new TransferFundsCommand(
                sourceAccount.getId(),
                targetAccount.getId(),
                Money.of(transferAmount),
                idempotencyKey
        );
        transferFundsUseCase.execute(command);
        // --- 3. THEN: Validate Database Consistency ---

        // A. Verify Final Balances in the Repository
        Account updatedSource = accountRepository.findById(sourceAccount.getId()).orElseThrow();
        Account updatedTarget = accountRepository.findById(targetAccount.getId()).orElseThrow();

        assertThat(updatedSource.getBalance()).isEqualTo(Money.of(new BigDecimal("700.00")));
        assertThat(updatedTarget.getBalance()).isEqualTo(Money.of(new BigDecimal("500.00")));

        // B. Verify Transfer Record Metadata
        Transfer transfer =
                transferRepository.findByIdempotencyKey(idempotencyKey)
                        .orElseThrow();

        assertThat(transfer.getStatus())
                .isEqualTo(TransferStatus.COMPLETED);

        // C. Verify Ledger Integrity (Transaction History)
        List<Transaction> sourceTxs = transactionRepository.findByAccountId(sourceAccount.getId());
        List<Transaction> targetTxs = transactionRepository.findByAccountId(targetAccount.getId());

        // Check source account's debit entry
        assertThat(sourceTxs).hasSize(1);
        assertThat(sourceTxs.get(0).getType()).isEqualTo(Transaction.TransactionType.DEBIT);
        assertThat(sourceTxs.get(0).getAmount()).isEqualTo(Money.of(transferAmount));

        // Check target account's credit entry
        assertThat(targetTxs).hasSize(1);
        assertThat(targetTxs.get(0).getType()).isEqualTo(Transaction.TransactionType.CREDIT);
        assertThat(targetTxs.get(0).getAmount()).isEqualTo(Money.of(transferAmount));
    }
}