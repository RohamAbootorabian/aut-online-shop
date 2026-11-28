package ir.aut.shop.service;

import ir.aut.shop.model.Money;
import ir.aut.shop.model.Order;
import ir.aut.shop.model.OrderId;
import ir.aut.shop.model.Payment;
import ir.aut.shop.model.PaymentId;
import ir.aut.shop.model.UserId;
import java.time.LocalDateTime;
import ir.aut.shop.repository.PaymentRepository;
import java.util.Comparator;
import java.util.List;

/**
 * Paying for an order and recording what the gateway answered
 * (user stories 23, 24 and 25).
 *
 * <p>The amount always comes from the order, never from the caller, which is how user
 * stories 22 and 23 keep a customer from choosing what to pay.
 */
public class PaymentService {

    private final PaymentRepository payments;
    private final PayableOrders orders;
    private final PaymentGateway gateway;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds the service over the given payment store and gateway.
     *
     * @param payments where payment attempts are kept
     * @param orders supplies the order being paid for, already checked to be the payer's
     * @param gateway the outside party that moves the money
     */
    public PaymentService(PaymentRepository payments, PayableOrders orders,
            PaymentGateway gateway) {
        if (payments == null || orders == null || gateway == null) {
            throw new IllegalArgumentException("PaymentService is missing a dependency");
        }
        this.payments = payments;
        this.orders = orders;
        this.gateway = gateway;
    }

    /**
     * REQUIRES: orderId and payerId are non-null.
     * MODIFIES: the payment store, and the order when the gateway approves.
     * EFFECTS: Asks the gateway for the order's exact total and records the attempt.
     * On approval the order becomes paid; on refusal it stays payable so the customer may
     * try again (user story 24). Throws IllegalArgumentException when the order is not the
     * payer's, and IllegalStateException when it is no longer payable, which is what stops
     * a paid order being paid twice (user story 23).
     *
     * @param orderId the order to pay for
     * @param payerId who is paying
     * @return the recorded attempt, successful or failed
     */
    public Payment pay(OrderId orderId, UserId payerId) {
        Order order = orders.findOrderOfCustomer(orderId, payerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order " + orderId + " does not belong to this customer"));
        if (!order.isPayable()) {
            throw new IllegalStateException(
                    "Order " + orderId + " is already " + order.status());
        }

        Payment attempt = new Payment(PaymentId.newId(), orderId, order.totalAmount(),
                LocalDateTime.now());
        payments.save(attempt.id(), attempt);

        GatewayResult result = gateway.authorize(orderId, order.totalAmount());
        if (result.approved()) {
            confirm(attempt, order, result.reference());
        } else {
            attempt.markFailed();
        }
        return attempt;
    }

    /**
     * REQUIRES: orderId is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns every attempt made on that order, newest first.
     *
     * @param orderId whose attempts to list
     * @return the payment attempts
     */
    public List<Payment> paymentsOf(OrderId orderId) {
        return payments.findAll().stream()
                .filter(payment -> payment.orderId().equals(orderId))
                .sorted(Comparator.comparing(Payment::attemptedAt).reversed())
                .toList();
    }

    /**
     * Confirms an approved attempt.
     *
     * <p>User story 25 asks for two checks before an order may be called paid: the gateway
     * must have approved, and the amount taken must equal the amount owed.
     */
    private void confirm(Payment attempt, Order order, String reference) {
        Money paid = attempt.amount();
        if (!paid.equals(order.totalAmount())) {
            attempt.markFailed();
            throw new IllegalStateException("Paid " + paid + " but the order costs "
                    + order.totalAmount());
        }
        attempt.markSuccessful(reference);
        order.markPaid();
    }
}
