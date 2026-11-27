package ir.aut.shop.model;

import java.util.UUID;

/**
 * Identity of a {@link User}.
 *
 * <p>A typed identity keeps the compiler from accepting, say, a {@code UserId} where a
 * {@code ProductId} is required.
 *
 * @param value the non-blank identifier text
 */
public record UserId(String value) {

    public UserId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UserId must not be blank");
        }
    }

    /**
     * Returns a fresh, unique identity.
     *
     * @return a new UserId backed by a random UUID
     */
    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
