package ir.aut.shop.service;

/**
 * What the payment gateway answered for one attempt.
 *
 * @param approved whether the gateway accepted the payment
 * @param reference the gateway's reference when approved, or the refusal reason when not
 */
public record GatewayResult(boolean approved, String reference) {

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the result, or throws IllegalArgumentException when the reference is
     * blank.
     */
    public GatewayResult {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Gateway reference must not be blank");
        }
        reference = reference.trim();
    }

    /**
     * Builds the answer for an accepted payment.
     *
     * @param reference the gateway's reference for the payment
     * @return an approving result
     */
    public static GatewayResult approved(String reference) {
        return new GatewayResult(true, reference);
    }

    /**
     * Builds the answer for a refused payment.
     *
     * @param reason why the gateway refused
     * @return a refusing result
     */
    public static GatewayResult refused(String reason) {
        return new GatewayResult(false, reason);
    }
}
