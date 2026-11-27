package ir.aut.shop.repository.memory;

import ir.aut.shop.model.OrderId;
import ir.aut.shop.model.Order;
import ir.aut.shop.repository.OrderRepository;

/**
 * Placed orders kept in memory.
 */
public class InMemoryOrderRepository extends InMemoryRepository<OrderId, Order> implements OrderRepository {
}
