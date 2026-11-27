package ir.aut.shop.repository;

import ir.aut.shop.model.OrderId;
import ir.aut.shop.model.Order;

/**
 * Where placed orders are kept.
 */
public interface OrderRepository extends Repository<OrderId, Order> {
}
