package ir.aut.shop.model;

import java.time.LocalDateTime;

/**
 * One attempt to pay for an order (user stories 23, 24 and 25).
 *
 * <p>An order may carry several attempts, because a failed payment may be retried while
 * the order is still payable.
 */
public class Payment {

    private final PaymentId id;
    private final OrderId orderId;
    private final Money amount;
    private final LocalDateTime attemptedAt;
    private PaymentStatus status;
    private String gatewayReference;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds a pending attempt, or throws IllegalArgumentException when an
     * argument is missing.
     *
     * @param id the identity of this attempt
     * @param orderId the order being paid for
     * @param amount the amount sent to the gateway
     * @param attemptedAt when the attempt started
     */
    public Payment(PaymentId id, OrderId orderId, Money amount, LocalDateTime attemptedAt) {
        if (id == null || orderId == null || amount == null || attemptedAt == null) {
            throw new IllegalArgumentException("Payment is missing a required field");
        }
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.attemptedAt = attemptedAt;
        this.status = PaymentStatus.PENDING;
        checkRep();
    }

    public PaymentId id() {
        return id;
    }

    public OrderId orderId() {
        return orderId;
    }

    public Money amount() {
        return amount;
    }

    public LocalDateTime attemptedAt() {
        return attemptedAt;
    }

    public PaymentStatus status() {
        return status;
    }

    /**
     * Returns the reference the gateway gave for this attempt.
     *
     * @return the gateway reference, or null while the attempt is still pending
     */
    public String gatewayReference() {
        return gatewayReference;
    }

    /**
     * Reports whether the gateway confirmed this attempt.
     *
     * @return true when the payment succeeded
     */
    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESSFUL;
    }

    /**
     * REQUIRES: this attempt is still pending; reference is non-blank.
     * MODIFIES: this payment.
     * EFFECTS: Records the gateway's confirmation, or throws IllegalStateException when
     * the attempt already has an outcome.
     *
     * @param reference the gateway's reference for the confirmed payment
     */
    public void markSuccessful(String reference) {
        requirePending();
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Gateway reference must not be blank");
        }
        this.status = PaymentStatus.SUCCESSFUL;
        this.gatewayReference = reference.trim();
        checkRep();
    }

    /**
     * REQUIRES: this attempt is still pending.
     * MODIFIES: this payment.
     * EFFECTS: Records that the gateway refused the payment, or throws
     * IllegalStateException when the attempt already has an outcome.
     */
    public void markFailed() {
        requirePending();
        this.status = PaymentStatus.FAILED;
        checkRep();
    }

    private void requirePending() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment already finished with status " + status);
        }
    }

    // RI: id, orderId, amount and status are non-null;
    //     a successful payment always carries a gateway reference.
    private void checkRep() {
        assert id != null : "payment id is null";
        assert orderId != null : "payment order id is null";
        assert amount != null : "payment amount is null";
        assert status != null : "payment status is null";
        assert status != PaymentStatus.SUCCESSFUL || gatewayReference != null
                : "successful payment has no gateway reference";
    }
}
