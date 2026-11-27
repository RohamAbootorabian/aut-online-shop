package ir.aut.shop.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A non-negative amount of money in the single currency of this shop.
 *
 * <p>Immutable. Every arithmetic operation is a producer: it returns a new {@code Money}
 * and leaves both operands untouched.
 *
 * <p>The shop uses one currency with at most two decimal places, so no currency field is
 * carried here.
 *
 * @param amount the non-negative amount, scaled to two decimal places
 */
public record Money(BigDecimal amount) {

    private static final int DECIMAL_PLACES = 2;

    /** The zero amount, used as the starting point of every total. */
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    /**
     * REQUIRES: amount is non-null and non-negative.
     * EFFECTS: Builds the amount rounded to two decimal places, or throws
     * IllegalArgumentException when the amount is null or negative.
     */
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Money amount must not be null");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money amount must not be negative: " + amount);
        }
        amount = amount.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    /**
     * Builds an amount from its decimal text.
     *
     * @param amount decimal text such as {@code "19.90"}
     * @return the matching amount
     * @throws IllegalArgumentException if the text is not a non-negative decimal number
     */
    public static Money of(String amount) {
        try {
            return new Money(new BigDecimal(amount));
        } catch (NumberFormatException cause) {
            throw new IllegalArgumentException("Not a valid amount: " + amount, cause);
        }
    }

    /**
     * Returns the sum of this amount and another.
     *
     * @param other the amount to add
     * @return a new amount; neither operand changes
     */
    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    /**
     * Returns this amount reduced by another.
     *
     * @param other the amount to subtract
     * @return a new amount
     * @throws IllegalArgumentException if the result would be negative
     */
    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    /**
     * Returns this amount repeated a number of times.
     *
     * @param factor how many times to repeat the amount, zero or more
     * @return a new amount
     * @throws IllegalArgumentException if the factor is negative
     */
    public Money times(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Factor must not be negative: " + factor);
        }
        return new Money(amount.multiply(BigDecimal.valueOf(factor)));
    }

    /**
     * Reports whether this amount is smaller than another.
     *
     * @param other the amount to compare against
     * @return true when this amount is strictly smaller
     */
    public boolean isLessThan(Money other) {
        return amount.compareTo(other.amount) < 0;
    }

    /**
     * Reports whether this amount is greater than another.
     *
     * @param other the amount to compare against
     * @return true when this amount is strictly greater
     */
    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
