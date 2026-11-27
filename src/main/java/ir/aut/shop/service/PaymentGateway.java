package ir.aut.shop.service;

import ir.aut.shop.model.Money;
import ir.aut.shop.model.OrderId;

/**
 * The outside party that actually moves the money (user stories 10 and 11 model it as an
 * actor of its own).
 *
 * <p>This interface is the boundary the shop hides behind. Which provider is used, how it
 * is reached and what it charges are decisions that may change; nothing above this line
 * depends on them.
 */
public interface PaymentGateway {

    /**
     * REQUIRES: orderId and amount are non-null.
     * MODIFIES: nothing inside the shop.
     * EFFECTS: Asks the gateway to take the amount for the order and returns its answer.
     *
     * @param orderId the order being paid for
     * @param amount how much to take
     * @return the gateway's answer, approving or refusing
     */
    GatewayResult authorize(OrderId orderId, Money amount);
}
