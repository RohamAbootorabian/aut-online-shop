package ir.aut.shop.service;

import ir.aut.shop.model.ProductId;
import ir.aut.shop.model.UserId;

/**
 * Whether a customer has actually bought a product.
 *
 * <p>User stories 30 and 31 let only a buyer review a product, so reviewing has exactly
 * one question for the ordering service. Everything else an order service can do — placing
 * orders, cancelling them, listing an admin's view of them — is none of reviewing's
 * business, and this interface is the shape of that sentence.
 */
@FunctionalInterface
public interface PurchaseHistory {

    /**
     * REQUIRES: both arguments are non-null.
     * MODIFIES: nothing.
     * EFFECTS: Reports whether the customer has a paid order holding that product.
     *
     * @param customerId who is asking
     * @param productId the product in question
     * @return true when they have bought it
     */
    boolean hasPurchased(UserId customerId, ProductId productId);
}
