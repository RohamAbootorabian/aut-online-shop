package ir.aut.shop.model;

import java.util.UUID;

/**
 * Identity of a {@link Payment}.
 *
 * <p>A typed identity keeps the compiler from accepting, say, a {@code UserId} where a
 * {@code ProductId} is required.
 *
 * @param value the non-blank identifier text
 */
public record PaymentId(String value) {

    public PaymentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PaymentId must not be blank");
        }
    }

    /**
     * Returns a fresh, unique identity.
     *
     * @return a new PaymentId backed by a random UUID
     */
    public static PaymentId newId() {
        return new PaymentId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
