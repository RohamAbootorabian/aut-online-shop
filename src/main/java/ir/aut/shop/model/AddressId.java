package ir.aut.shop.model;

import java.util.UUID;

/**
 * Identity of a {@link Address}.
 *
 * <p>A typed identity keeps the compiler from accepting, say, a {@code UserId} where a
 * {@code ProductId} is required.
 *
 * @param value the non-blank identifier text
 */
public record AddressId(String value) {

    public AddressId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AddressId must not be blank");
        }
    }

    /**
     * Returns a fresh, unique identity.
     *
     * @return a new AddressId backed by a random UUID
     */
    public static AddressId newId() {
        return new AddressId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
