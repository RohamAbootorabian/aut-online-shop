package ir.aut.shop.model;

import java.time.LocalDateTime;

/**
 * One recorded change to a product's stock (user story 29, "the change must be logged").
 *
 * <p>Immutable: a log entry describes something that already happened, so nothing about it
 * may be rewritten afterwards.
 *
 * @param productId the product whose stock changed
 * @param delta how many units were added, or removed when negative
 * @param reason why the admin changed the stock
 * @param changedBy the admin who made the change
 * @param changedAt when the change was made
 */
public record StockChange(ProductId productId, int delta, String reason, UserId changedBy,
        LocalDateTime changedAt) {

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the log entry, or throws IllegalArgumentException when a field is
     * missing, the reason is blank or the delta is zero.
     */
    public StockChange {
        if (productId == null || changedBy == null || changedAt == null) {
            throw new IllegalArgumentException("Stock change must name product, admin and time");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Stock change reason must not be blank");
        }
        if (delta == 0) {
            throw new IllegalArgumentException("Stock change delta must not be zero");
        }
        reason = reason.trim();
    }

    /**
     * Reports whether this entry added units to the shelf.
     *
     * @return true when stock went up
     */
    public boolean isIncrease() {
        return delta > 0;
    }
}
