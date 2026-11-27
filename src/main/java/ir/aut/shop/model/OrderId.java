package ir.aut.shop.model;

import java.util.UUID;

/**
 * Identity of a {@link Order}.
 *
 * <p>A typed identity keeps the compiler from accepting, say, a {@code UserId} where a
 * {@code ProductId} is required.
 *
 * @param value the non-blank identifier text
 */
public record OrderId(String value) {

    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId must not be blank");
        }
    }

    /**
     * Returns a fresh, unique identity.
     *
     * @return a new OrderId backed by a random UUID
     */
    public static OrderId newId() {
        return new OrderId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
