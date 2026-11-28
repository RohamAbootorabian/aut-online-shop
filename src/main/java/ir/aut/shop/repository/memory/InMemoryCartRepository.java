package ir.aut.shop.repository.memory;

import ir.aut.shop.model.UserId;
import ir.aut.shop.model.Cart;
import ir.aut.shop.repository.CartRepository;

/**
 * Shopping carts kept in memory.
 */
public class InMemoryCartRepository extends InMemoryRepository<UserId, Cart> implements CartRepository {
}
