package ir.aut.shop.model;

/**
 * The outcome of one payment attempt (user stories 23, 24 and 25).
 */
public enum PaymentStatus {

    /** Sent to the gateway, no answer yet. */
    PENDING,

    /** The gateway confirmed the payment. */
    SUCCESSFUL,

    /** The gateway refused the payment; the customer may try again. */
    FAILED
}
