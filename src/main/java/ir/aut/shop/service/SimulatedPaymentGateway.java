package ir.aut.shop.service;

import ir.aut.shop.model.Money;
import ir.aut.shop.model.OrderId;
import java.util.UUID;

/**
 * A stand-in gateway for running the shop without a real provider.
 *
 * <p>Its answer is set from outside rather than guessed, so the two separate domain
 * stories can both be walked through on demand: story 10 with an approving gateway and
 * story 11 with a refusing one.
 */
public class SimulatedPaymentGateway implements PaymentGateway {

    private boolean approving = true;
    private String refusalReason = "Insufficient funds";

    /**
     * MODIFIES: this gateway.
     * EFFECTS: Makes every later attempt succeed.
     */
    public void approveEverything() {
        this.approving = true;
    }

    /**
     * REQUIRES: reason is non-blank.
     * MODIFIES: this gateway.
     * EFFECTS: Makes every later attempt fail with the given reason.
     *
     * @param reason what to report as the cause of refusal
     */
    public void refuseEverything(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Refusal reason must not be blank");
        }
        this.approving = false;
        this.refusalReason = reason.trim();
    }

    /**
     * Reports whether the simulated gateway currently approves payments.
     *
     * @return true when attempts will be approved
     */
    public boolean isApproving() {
        return approving;
    }

    @Override
    public GatewayResult authorize(OrderId orderId, Money amount) {
        if (orderId == null || amount == null) {
            throw new IllegalArgumentException("Gateway needs an order and an amount");
        }
        if (!approving) {
            return GatewayResult.refused(refusalReason);
        }
        return GatewayResult.approved("SIM-" + UUID.randomUUID());
    }
}
