package ir.aut.shop.model;

import java.util.UUID;

/**
 * Identity of a {@link Review}.
 *
 * <p>A typed identity keeps the compiler from accepting, say, a {@code UserId} where a
 * {@code ProductId} is required.
 *
 * @param value the non-blank identifier text
 */
public record ReviewId(String value) {

    public ReviewId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ReviewId must not be blank");
        }
    }

    /**
     * Returns a fresh, unique identity.
     *
     * @return a new ReviewId backed by a random UUID
     */
    public static ReviewId newId() {
        return new ReviewId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
