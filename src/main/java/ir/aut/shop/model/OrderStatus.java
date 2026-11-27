package ir.aut.shop.model;

/**
 * Where an order stands in its life (user stories 23, 24 and 26).
 */
public enum OrderStatus {

    /** Placed but not paid for; it may still be paid or retried. */
    AWAITING_PAYMENT,

    /** Payment was confirmed by the gateway; it may not be paid again. */
    PAID,

    /** Abandoned; it may no longer be paid. */
    CANCELLED
}
