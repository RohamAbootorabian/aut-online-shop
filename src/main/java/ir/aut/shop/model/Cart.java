package ir.aut.shop.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The products a customer has picked but not yet ordered (user stories 15 to 19).
 *
 * <p>Abstractly a cart is a mapping from product to a positive quantity, together with the
 * unit price captured when each product was first added. Whether that mapping is held as a
 * list or a map is this class's own business: every caller works through the operations
 * below, so the representation can change without touching the rest of the program.
 *
 * <p>AF(c) = { (p, q) | c.lines holds a key p whose CartLine has quantity q }.
 * A product absent from c.lines has quantity zero in the abstract cart.
 *
 * <p>RI: ownerId is non-null; no line is null; the key of every entry equals the product
 * id of that entry's line.
 */
public class Cart {

    private final UserId ownerId;
    private final Map<ProductId, CartLine> lines = new HashMap<>();

    /**
     * REQUIRES: ownerId is non-null.
     * EFFECTS: Builds an empty cart for one customer, or throws IllegalArgumentException
     * when the owner is missing.
     *
     * @param ownerId the customer this cart belongs to
     */
    public Cart(UserId ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Cart owner must not be null");
        }
        this.ownerId = ownerId;
        checkRep();
    }

    /**
     * Returns the customer this cart belongs to, which is how user story 16 keeps one
     * customer from reading another's cart.
     *
     * @return the owner's identity
     */
    public UserId ownerId() {
        return ownerId;
    }

    /**
     * Returns the lines of this cart, in no particular order.
     *
     * @return an unmodifiable copy; later changes to the cart do not change it
     */
    public List<CartLine> lines() {
        return List.copyOf(lines.values());
    }

    /**
     * Returns how many units of one product the cart holds.
     *
     * @param productId the product to ask about
     * @return the quantity, or zero when the product is not in the cart
     */
    public int quantityOf(ProductId productId) {
        CartLine line = lines.get(productId);
        return line == null ? 0 : line.quantity().value();
    }

    /**
     * Returns what the products in this cart cost in total (user story 16).
     *
     * <p>The shop works this figure out itself; a price typed by a customer is never
     * accepted.
     *
     * @return the sum of every line total, or zero for an empty cart
     */
    public Money itemsSubtotal() {
        return lines.values().stream()
                .map(CartLine::lineTotal)
                .reduce(Money.ZERO, Money::plus);
    }

    /**
     * Reports whether the cart holds nothing.
     *
     * @return true when there are no lines
     */
    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /**
     * REQUIRES: product and quantity are non-null.
     * MODIFIES: this cart.
     * EFFECTS: After the call, quantityOf(product.id()) equals its previous value plus
     * quantity. A product already in the cart keeps the unit price captured when it was
     * first added (user story 15: a repeated product adds up rather than overwriting).
     * Other lines are unchanged.
     *
     * @param product the product being added
     * @param quantity how many units to add
     */
    public void addProduct(Product product, Quantity quantity) {
        if (product == null || quantity == null) {
            throw new IllegalArgumentException("Product and quantity must not be null");
        }
        CartLine existing = lines.get(product.id());
        if (existing == null) {
            lines.put(product.id(), CartLine.of(product, quantity));
        } else {
            lines.put(product.id(), existing.withQuantity(existing.quantity().plus(quantity)));
        }
        checkRep();
    }

    /**
     * REQUIRES: the product is in the cart; quantity is non-null.
     * MODIFIES: this cart.
     * EFFECTS: After the call, quantityOf(productId) equals quantity, or throws
     * IllegalArgumentException when the product is not in the cart. Because Quantity is
     * always positive, this can never empty a line; removing is removeProduct's job
     * (user story 17).
     *
     * @param productId the product to change
     * @param quantity the quantity to set
     */
    public void changeQuantity(ProductId productId, Quantity quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity must not be null");
        }
        CartLine existing = lines.get(productId);
        if (existing == null) {
            throw new IllegalArgumentException("Cart holds no product " + productId);
        }
        lines.put(productId, existing.withQuantity(quantity));
        checkRep();
    }

    /**
     * REQUIRES: productId is non-null.
     * MODIFIES: this cart.
     * EFFECTS: After the call, quantityOf(productId) is zero. Removing a product the cart
     * does not hold leaves the cart unchanged (user story 18).
     *
     * @param productId the product to drop
     */
    public void removeProduct(ProductId productId) {
        lines.remove(productId);
        checkRep();
    }

    /**
     * MODIFIES: this cart.
     * EFFECTS: After the call the cart is empty and its subtotal is zero (user story 19).
     */
    public void clear() {
        lines.clear();
        checkRep();
    }

    private void checkRep() {
        assert ownerId != null : "cart owner is null";
        for (Map.Entry<ProductId, CartLine> entry : lines.entrySet()) {
            CartLine line = entry.getValue();
            assert line != null : "null cart line";
            assert line.productId().equals(entry.getKey())
                    : "map key does not match product id";
        }
    }
}
