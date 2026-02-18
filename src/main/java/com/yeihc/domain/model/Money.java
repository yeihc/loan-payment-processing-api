package com.yeihc.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object representing monetary amounts within the domain.
 * * DESIGN DECISIONS:
 * 1. Immutability: The class is final and has no setters to prevent side effects.
 * 2. Precision: Enforces 2 decimal places to ensure financial consistency.
 * 3. Rounding: Uses HALF_EVEN (Banker's Rounding) to minimize cumulative errors in large datasets.
 * 4. JPA Integration: Marked as @Embeddable to be stored as a flat column in parent entity tables.
 * Why is Money class and not record?
 * A Money(BigDecimal value) record automatically exposes the value() getter. There is no way to "hide" it without it ceasing to be a record.
 */
@Embeddable
public final class Money {

    // Financial standards: 2 decimal places for most currencies
    private static final int SCALE = 2;

    // Banker's Rounding: Rounds towards the nearest even neighbor (statistically unbiased)
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    @Column(name = "balance_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal value;

    /**
     * Required by JPA/Hibernate proxy mechanism.
     * Protected to prevent direct instantiation from other layers.
     */
    protected Money() {
        // For JPA
    }

    /**
     * Primary constructor ensuring data integrity upon creation.
     */
    private Money(BigDecimal value) {
        Objects.requireNonNull(value, "Money value cannot be null");
        this.value = value.setScale(SCALE, ROUNDING);
    }

    /**
     * Factory method to create Money from a BigDecimal.
     */
    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    /**
     * Utility method for initializing zero balances.
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    // --- Arithmetic Operations (Returning new instances to maintain immutability) ---

    public Money add(Money other) {
        return new Money(this.value.add(other.value));
    }

    public Money subtract(Money other) {
        return new Money(this.value.subtract(other.value));
    }

    // Standard getters, equals, and hashCode
    public BigDecimal getValue() { return value; }

    // --- Comparison Operations ---

    public boolean isLessThan(Money other) {
        return this.value.compareTo(other.value) < 0;
    }

    /**
     * Compares if this amount is less than or equal to another.
     * Uses compareTo to ignore scale differences (e.g., 1.0 vs 1.00).
     */
    public boolean isLessThanOrEqual(Money other) {
        return this.value.compareTo(other.value) <= 0;
    }

    public boolean isGreaterThan(Money other) {
        return this.value.compareTo(other.value) > 0;
    }

    /**
     * Uses signum() for performance as it avoids object comparison overhead.
     * @return true if amount is less than zero.
     */
    public boolean isNegative() {
        return this.value.signum() < 0;
    }

    /**
     * Returns a string representation without scientific notation.
     */
    @Override
    public String toString() {
        return value.toPlainString();
    }

    /**
     * Equality by value, not by reference.
     * Note: Uses compareTo() instead of equals() because BigDecimal.equals()
     * considers scale (e.g., 1.0 != 1.00), but compareTo() does not.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return value.compareTo(money.value) == 0;
    }

    /**
     * Ensures consistent hash coding by stripping trailing zeros.
     */
    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }
}