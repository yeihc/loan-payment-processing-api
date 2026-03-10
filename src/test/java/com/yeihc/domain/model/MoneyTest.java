package com.yeihc.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the Money Value Object.
 * <p>Location: {@code src/test/java/com/yeihc/domain/model/MoneyTest.java}</p>
 *
 * <p><b>DESIGN PRINCIPLES TESTED:</b></p>
 * <ul>
 * <li><b>Financial Accuracy:</b> Ensures all calculations use a scale of 2 with {@code HALF_EVEN} rounding.</li>
 * <li><b>Value Semantics:</b> Verifies that two Money objects are equal if their values are numerically identical.</li>
 * <li><b>Immutability:</b> Confirms that arithmetic operations return new instances without mutating the original.</li>
 * <li><b>Self-Validation:</b> Validates that the object cannot be created in an invalid state (null).</li>
 * </ul>
 */
@DisplayName("Unit Test: Money Value Object")
class MoneyTest {

    /**
     * Verifies that the Money object normalizes any input to exactly 2 decimal places.
     * Use of HALF_EVEN (Banker's Rounding) is standard in financial applications.
     */
    @Test
    @DisplayName("Should enforce a fixed scale of 2 decimal places (HALF_EVEN rounding)")
    void shouldEnforceFixedScale() {
        // Given
        BigDecimal rawAmount = new BigDecimal("100.555");

        // When
        Money money = Money.of(rawAmount);

        // Then
        assertThat(money.getValue().scale()).isEqualTo(2);
        assertThat(money.getValue()).isEqualByComparingTo(new BigDecimal("100.56"));
    }

    /**
     * Ensures that Value Object equality is based on the numeric value,
     * regardless of the original scale provided in the constructor.
     */
    @Test
    @DisplayName("Should normalize scale consistently (1.0 equals 1.00 by value)")
    void shouldTreatSameNumericValueAsEqualRegardlessOfScale() {
        Money a = Money.of(new BigDecimal("1.0"));
        Money b = Money.of(new BigDecimal("1.00"));

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    /**
     * Group of tests focusing on the rich domain language for comparisons.
     */
    @Nested
    @DisplayName("Comparison Logic")
    class ComparisonTests {

        @ParameterizedTest(name = "{0} comparedTo {1} should be {2}")
        @CsvSource({
                "150.00, 100.00, 1",
                "50.00, 100.00, -1",
                "100.00, 100.00, 0"
        })
        void shouldCompareAmountsCorrectly(String first, String second, int expected) {
            Money firstMoney = Money.of(new BigDecimal(first));
            Money secondMoney = Money.of(new BigDecimal(second));

            assertThat(firstMoney.getValue().compareTo(secondMoney.getValue()))
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Should identify zero and negative states")
        void shouldIdentifyStates() {
            assertThat(Money.zero().isZero()).isTrue();
            assertThat(Money.of(new BigDecimal("-1.00")).isNegative()).isTrue();

            Money positive = Money.of(new BigDecimal("1.00"));
            assertThat(positive.isZero()).isFalse();
            assertThat(positive.isNegative()).isFalse();
        }

        @Test
        @DisplayName("Should evaluate domain-specific comparators (<= and >)")
        void shouldEvaluateComparators() {
            Money a = Money.of(new BigDecimal("10.00"));
            Money b = Money.of(new BigDecimal("10.00"));
            Money c = Money.of(new BigDecimal("9.99"));

            assertThat(a.isLessThanOrEqual(b)).isTrue();
            assertThat(c.isLessThanOrEqual(a)).isTrue();
            assertThat(a.isGreaterThan(c)).isTrue();
        }
    }

    /**
     * Verifies that arithmetic logic follows the Immutable Pattern.
     */
    @Nested
    @DisplayName("Arithmetic Operations")
    class ArithmeticTests {

        @Test
        @DisplayName("Addition should return a NEW Money instance")
        void shouldAddMoney() {
            Money initial = Money.of(new BigDecimal("100.00"));
            Money toAdd = Money.of(new BigDecimal("50.00"));

            Money result = initial.add(toAdd);

            assertThat(result).isEqualTo(Money.of(new BigDecimal("150.00")));
            assertThat(result).isNotSameAs(initial); // Reference check
            assertThat(initial).isEqualTo(Money.of(new BigDecimal("100.00"))); // Immutability check
        }

        @Test
        @DisplayName("Subtraction should return a NEW Money instance")
        void shouldSubtractMoney() {
            Money initial = Money.of(new BigDecimal("100.00"));
            Money toSub = Money.of(new BigDecimal("40.00"));

            Money result = initial.subtract(toSub);

            assertThat(result).isEqualTo(Money.of(new BigDecimal("60.00")));
            assertThat(result).isNotSameAs(initial);
            assertThat(initial).isEqualTo(Money.of(new BigDecimal("100.00")));
        }
    }

    /**
     * Ensures the fail-fast principle during object instantiation.
     */
    @Test
    @DisplayName("Should throw NullPointerException for null amounts")
    void shouldHandleNull() {
        assertThatThrownBy(() -> Money.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Money value cannot be null");
    }
}