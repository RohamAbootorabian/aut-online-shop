package ir.aut.shop.model;

/**
 * How many units of a product the warehouse holds.
 *
 * <p>Stock is never negative. User story 29 states this twice: the admin may not set a
 * negative stock, and the system must refuse a reduction that would fall below zero. Both
 * rules live here, so no caller has to remember them.
 *
 * @param value the number of units in stock, zero or more
 */
public record StockCount(int value) {

    /** An empty shelf, the starting stock of a newly created product. */
    public static final StockCount NONE = new StockCount(0);

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the stock count, or throws IllegalArgumentException when the value
     * is negative.
     */
    public StockCount {
        if (value < 0) {
            throw new IllegalArgumentException("Stock must not be negative: " + value);
        }
    }

    /**
     * Reports whether this stock can satisfy a wanted quantity.
     *
     * @param quantity the quantity a customer wants
     * @return true when the stock is at least the wanted quantity
     */
    public boolean canCover(Quantity quantity) {
        return value >= quantity.value();
    }

    /**
     * Returns the stock left after taking out a quantity.
     *
     * @param quantity the quantity to take out
     * @return a new stock count; this one does not change
     * @throws IllegalArgumentException if the stock cannot cover the quantity
     */
    public StockCount decreaseBy(Quantity quantity) {
        if (!canCover(quantity)) {
            throw new IllegalArgumentException(
                    "Not enough stock: have " + value + ", need " + quantity.value());
        }
        return new StockCount(value - quantity.value());
    }

    /**
     * Returns the stock after adding units.
     *
     * @param amount how many units to add, zero or more
     * @return a new stock count
     * @throws IllegalArgumentException if the amount is negative
     */
    public StockCount increaseBy(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Added amount must not be negative: " + amount);
        }
        return new StockCount(value + amount);
    }

    /**
     * Reports whether the shelf is empty.
     *
     * @return true when no units remain
     */
    public boolean isEmpty() {
        return value == 0;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
