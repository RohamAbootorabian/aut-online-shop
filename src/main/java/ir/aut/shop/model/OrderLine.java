package ir.aut.shop.model;

/**
 * One product and its quantity frozen onto a placed {@link Order}.
 *
 * <p>Immutable without exception. An order is a historical document: user story 35 says
 * past orders must survive a product being switched off, so nothing here may change after
 * the order is placed.
 *
 * @param productId the ordered product
 * @param productName the product name at the time of ordering
 * @param unitPrice the price of one unit at the time of ordering
 * @param quantity how many units were ordered
 */
public record OrderLine(ProductId productId, String productName, Money unitPrice,
        Quantity quantity) {

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the line, or throws IllegalArgumentException when a field is missing
     * or the product name is blank.
     */
    public OrderLine {
        if (productId == null || unitPrice == null || quantity == null) {
            throw new IllegalArgumentException("Order line is missing a required field");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Order line product name must not be blank");
        }
        productName = productName.trim();
    }

    /**
     * Freezes a cart line onto an order.
     *
     * @param cartLine the line being ordered
     * @return the matching order line
     */
    public static OrderLine from(CartLine cartLine) {
        return new OrderLine(cartLine.productId(), cartLine.productName(),
                cartLine.unitPrice(), cartLine.quantity());
    }

    /**
     * Returns what this line cost in total.
     *
     * @return the unit price repeated for every ordered unit
     */
    public Money lineTotal() {
        return unitPrice.times(quantity.value());
    }
}
