package ir.aut.shop.model;

import java.util.UUID;

/**
 * Identity of a {@link Product}.
 *
 * <p>A typed identity keeps the compiler from accepting, say, a {@code UserId} where a
 * {@code ProductId} is required.
 *
 * @param value the non-blank identifier text
 */
public record ProductId(String value) {

    public ProductId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ProductId must not be blank");
        }
    }

    /**
     * Returns a fresh, unique identity.
     *
     * @return a new ProductId backed by a random UUID
     */
    public static ProductId newId() {
        return new ProductId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
