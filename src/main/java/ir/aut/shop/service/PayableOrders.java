package ir.aut.shop.service;

import ir.aut.shop.model.Order;
import ir.aut.shop.model.OrderId;
import ir.aut.shop.model.UserId;
import java.util.Optional;

/**
 * Finding the one order a customer is about to pay for.
 *
 * <p>Paying needs to fetch an order and be sure it belongs to the person paying. The
 * ownership check is part of the question, not something the payment service adds
 * afterwards, which is why it is one method and not two.
 */
@FunctionalInterface
public interface PayableOrders {

    /**
     * REQUIRES: both arguments are non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the order when it exists and belongs to that customer, and empty
     * otherwise. One customer therefore cannot reach another's order through this
     * interface (user story 27).
     *
     * @param orderId the order to fetch
     * @param customerId who it must belong to
     * @return the order, or empty
     */
    Optional<Order> findOrderOfCustomer(OrderId orderId, UserId customerId);
}
