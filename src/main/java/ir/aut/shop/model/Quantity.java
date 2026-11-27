package ir.aut.shop.model;

/**
 * How many units of one product a customer wants.
 *
 * <p>A quantity is always strictly positive. Making that a property of the type rather
 * than a precondition repeated on every operation means no part of the program can build
 * an invalid quantity in the first place (user story 17).
 *
 * <p>This is deliberately a different type from {@link StockCount}: stock may legitimately
 * fall to zero, a wanted quantity may not.
 *
 * @param value the number of units, strictly positive
 */
public record Quantity(int value) {

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the quantity, or throws IllegalArgumentException when the value is
     * zero or negative.
     */
    public Quantity {
        if (value <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + value);
        }
    }

    /**
     * Returns the total of this quantity and another.
     *
     * @param other the quantity to add
     * @return a new quantity; neither operand changes
     */
    public Quantity plus(Quantity other) {
        return new Quantity(value + other.value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
