package ir.aut.shop.service;

import ir.aut.shop.model.Cart;
import ir.aut.shop.model.UserId;

/**
 * The two things placing an order needs a cart to do.
 *
 * <p>Ordering reads the customer's cart and, once the order exists, empties it. It never
 * adds a line, changes a quantity or removes one: those belong to the customer, not to
 * checkout. Naming only these two operations means the ordering service could not perform
 * the others even if a later change tempted it to.
 */
public interface CheckoutBasket {

    /**
     * REQUIRES: ownerId is non-null.
     * MODIFIES: the cart store, when the customer had no cart yet.
     * EFFECTS: Returns the customer's cart, creating an empty one the first time.
     *
     * @param ownerId whose cart to fetch
     * @return that customer's cart
     */
    Cart cartOf(UserId ownerId);

    /**
     * REQUIRES: ownerId is non-null.
     * MODIFIES: the customer's cart.
     * EFFECTS: Removes every line from the cart.
     *
     * @param ownerId whose cart to empty
     */
    void clearCart(UserId ownerId);
}
