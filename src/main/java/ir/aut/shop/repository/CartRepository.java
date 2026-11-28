package ir.aut.shop.repository;

import ir.aut.shop.model.UserId;
import ir.aut.shop.model.Cart;

/**
 * Where shopping carts are kept, one per customer.
 */
public interface CartRepository extends Repository<UserId, Cart> {
}
