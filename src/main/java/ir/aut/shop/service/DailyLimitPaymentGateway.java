package ir.aut.shop.service;

import ir.aut.shop.model.Money;
import ir.aut.shop.model.OrderId;
import java.util.UUID;

/**
 * A gateway that will not take more than a set amount in one go.
 *
 * <p>Real providers have ceilings, so this is the second implementation the shop can be
 * run against. It exists to make the substitution principle something the code shows
 * rather than something the document claims: it behaves differently from
 * {@link SimulatedPaymentGateway}, and the shop above it does not change by a line.
 *
 * <p>Note what it does <em>not</em> do. An amount over the ceiling is refused with a
 * reason, not thrown about. Throwing would be a stronger precondition than
 * {@link PaymentGateway} states — the caller would have to learn a new failure that the
 * interface never warned it of, and the two gateways would stop being interchangeable.
 * That is exactly the mistake the contract is written to prevent.
 */
public class DailyLimitPaymentGateway implements PaymentGateway {

    private final Money ceiling;

    /**
     * REQUIRES: ceiling is non-null.
     * EFFECTS: Builds a gateway that approves any amount up to and including the ceiling.
     *
     * @param ceiling the most this gateway will take in one payment
     */
    public DailyLimitPaymentGateway(Money ceiling) {
        if (ceiling == null) {
            throw new IllegalArgumentException("A payment ceiling must be given");
        }
        this.ceiling = ceiling;
    }

    /**
     * REQUIRES: nothing.
     * MODIFIES: nothing.
     * EFFECTS: Returns the most this gateway will take in one payment.
     *
     * @return the ceiling
     */
    public Money ceiling() {
        return ceiling;
    }

    @Override
    public GatewayResult authorize(OrderId orderId, Money amount) {
        if (orderId == null || amount == null) {
            throw new IllegalArgumentException("Gateway needs an order and an amount");
        }
        if (amount.amount().compareTo(ceiling.amount()) > 0) {
            return GatewayResult.refused(
                    "This card will not take more than " + ceiling + " in one payment");
        }
        return GatewayResult.approved("LIMIT-" + UUID.randomUUID());
    }
}
