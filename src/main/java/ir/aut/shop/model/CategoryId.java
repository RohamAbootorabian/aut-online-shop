package ir.aut.shop.model;

import java.util.UUID;

/**
 * Identity of a {@link Category}.
 *
 * <p>A typed identity keeps the compiler from accepting, say, a {@code UserId} where a
 * {@code ProductId} is required.
 *
 * @param value the non-blank identifier text
 */
public record CategoryId(String value) {

    public CategoryId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CategoryId must not be blank");
        }
    }

    /**
     * Returns a fresh, unique identity.
     *
     * @return a new CategoryId backed by a random UUID
     */
    public static CategoryId newId() {
        return new CategoryId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
