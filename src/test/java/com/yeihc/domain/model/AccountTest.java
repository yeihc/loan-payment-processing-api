package com.yeihc.domain.model;

import com.yeihc.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

/**
 * Unit tests for the Account Aggregate Root.
 * <p>Location: {@code src/test/java/com/yeihc/domain/model/AccountTest.java}</p>
 *
 * <p><b>TESTING PHILOSOPHY:</b></p>
 * <ul>
 * <li><b>Encapsulated Logic:</b> We verify that the Account manages its own balance
 * and state transitions according to business rules.</li>
 * <li><b>Ledger Integrity:</b> Validates that every financial movement (debit/credit)
 * correctly produces a Transaction record (Event/Audit trail).</li>
 * <li><b>Robustness:</b> Ensures that DomainExceptions are thrown when business
 * invariants are violated (e.g., insufficient funds).</li>
 * </ul>
 */
@DisplayName("Unit Test: Account Aggregate")
class AccountTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    /**
     * Focuses on balance modifications and side-effect generation.
     */
    @Nested
    @DisplayName("Balance Operations")
    class BalanceTests {

        @Test
        @DisplayName("Should credit amount and generate CREDIT transaction")
        void shouldCreditAmount() {
            // Given: An account with 100.00
            Account account = createAccount("100.00");

            // When: Crediting 50.00
            Transaction tx = account.credit(
                    Money.of(new BigDecimal("50.00")),
                    "Test credit"
            );

            // Then: Balance must be updated and a CREDIT transaction returned
            assertThat(account.getBalance())
                    .isEqualTo(Money.of(new BigDecimal("150.00")));

            assertThat(tx.getType())
                    .isEqualTo(Transaction.TransactionType.CREDIT);
            assertThat(tx.getAmount()).isEqualTo(Money.of(new BigDecimal("50.00")));
        }

        @Test
        @DisplayName("Should debit amount with sufficient funds and generate DEBIT transaction")
        void shouldDebitWithSufficientFunds() {
            // Given: An account with 100.00
            Account account = createAccount("100.00");

            // When: Debiting 40.00
            Transaction tx = account.debit(
                    Money.of(new BigDecimal("40.00")),
                    "Test debit"
            );

            // Then: Balance must decrease and a DEBIT transaction must be generated
            assertThat(account.getBalance())
                    .isEqualTo(Money.of(new BigDecimal("60.00")));

            assertThat(tx.getType())
                    .isEqualTo(Transaction.TransactionType.DEBIT);
        }

        @Test
        @DisplayName("Should fail when debiting more than available balance")
        void shouldFailDebitWithInsufficientFunds() {
            Account account = createAccount("50.00");

            DomainException exception = (DomainException) catchThrowable(() ->
                    account.debit(
                            Money.of(new BigDecimal("50.01")),
                            "Overdraft attempt"
                    )
            );

            assertThat(exception.getCode())
                    .isEqualTo("INSUFFICIENT_FUNDS");

            assertThat(exception.getMessage())
                    .isEqualTo("Not enough balance");
        }
    }

    /**
     * Focuses on the state machine of the Account.
     */
    @Nested
    @DisplayName("Account Lifecycle")
    class LifecycleTests {

        @Test
        @DisplayName("Should allow closing account when balance is zero")
        void shouldCloseAccountWithZeroBalance() {
            // Given: An empty account
            Account account = createAccount("0.00");

            // When: Closing the account
            account.close("Customer request");

            // Then: Status must be CLOSED
            assertThat(account.getStatus())
                    .isEqualTo(AccountStatus.CLOSED);
        }

        @Test
        @DisplayName("Should forbid closing account with remaining balance")
        void shouldFailClosureWithRemainingBalance() {
            Account account = createAccount("0.01");

            DomainException exception = (DomainException) catchThrowable(() ->
                    account.close("Invalid closure")
            );

            assertThat(exception.getCode())
                    .isEqualTo("INVALID_STATUS_CHANGE");

            assertThat(exception.getMessage())
                    .isEqualTo("Cannot close account with remaining balance");
        }

        @Test
        @DisplayName("Closing an already closed account should be idempotent")
        void closingAlreadyClosedAccountShouldDoNothing() {
            // Given: A closed account
            Account account = createAccount("0.00");
            account.close("First close");

            // When: Closing it again
            account.close("Second close");

            // Then: Status remains CLOSED without errors
            assertThat(account.getStatus())
                    .isEqualTo(AccountStatus.CLOSED);
        }
    }

    // ---------- Helper Method ----------

    /**
     * Utility to create a valid Account aggregate for testing.
     */
    private Account createAccount(String initialBalance) {
        return new Account(
                UUID.randomUUID(),
                CUSTOMER_ID,
                Money.of(new BigDecimal(initialBalance))
        );
    }
}