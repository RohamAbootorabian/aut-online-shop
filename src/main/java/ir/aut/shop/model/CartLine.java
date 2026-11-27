package ir.aut.shop.model;

/**
 * One product and its wanted quantity inside a {@link Cart}.
 *
 * <p>The unit price is copied in when the product is first added, so a later price change
 * by an admin (user story 34) does not silently rewrite what is already in the cart.
 *
 * <p>Immutable: changing a quantity produces a new line rather than editing this one.
 *
 * @param productId the product this line is about
 * @param productName the product name at the time it was added
 * @param unitPrice the price of one unit at the time it was added
 * @param quantity how many units the customer wants
 */
public record CartLine(ProductId productId, String productName, Money unitPrice,
        Quantity quantity) {

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the line, or throws IllegalArgumentException when a field is missing
     * or the product name is blank.
     */
    public CartLine {
        if (productId == null || unitPrice == null || quantity == null) {
            throw new IllegalArgumentException("Cart line is missing a required field");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Cart line product name must not be blank");
        }
        productName = productName.trim();
    }

    /**
     * Builds the line that stands for a product a customer just picked.
     *
     * @param product the product being added
     * @param quantity how many units the customer wants
     * @return a line holding the product's current name and price
     */
    public static CartLine of(Product product, Quantity quantity) {
        return new CartLine(product.id(), product.name(), product.price(), quantity);
    }

    /**
     * Returns what this line costs in total.
     *
     * @return the unit price repeated for every wanted unit
     */
    public Money lineTotal() {
        return unitPrice.times(quantity.value());
    }

    /**
     * Returns a copy of this line with a different quantity.
     *
     * @param newQuantity the quantity to use instead
     * @return a new line; this one does not change
     */
    public CartLine withQuantity(Quantity newQuantity) {
        return new CartLine(productId, productName, unitPrice, newQuantity);
    }
}
