package ir.aut.shop.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * The stored, one-way form of a user password.
 *
 * <p>User story 1 requires passwords to be stored encrypted. Raw passwords never enter
 * this type's state, and {@link User} exposes no getter for it, so a password cannot be
 * read back or printed anywhere in the program (user story 5).
 *
 * @param value the hex-encoded digest of the password
 */
public record PasswordHash(String value) {

    private static final String ALGORITHM = "SHA-256";
    private static final int MIN_RAW_LENGTH = 6;

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the hash holder, or throws IllegalArgumentException when the digest
     * text is blank.
     */
    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }
    }

    /**
     * Hashes a raw password chosen by a user.
     *
     * @param rawPassword the password as typed, at least six characters
     * @return the stored form of that password
     * @throws IllegalArgumentException if the password is too short
     */
    public static PasswordHash of(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_RAW_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_RAW_LENGTH + " characters");
        }
        return new PasswordHash(digestOf(rawPassword));
    }

    /**
     * Reports whether a typed password matches this stored hash.
     *
     * @param rawPassword the password as typed at login
     * @return true when the password hashes to the same value
     */
    public boolean matches(String rawPassword) {
        if (rawPassword == null) {
            return false;
        }
        return value.equals(digestOf(rawPassword));
    }

    private static String digestOf(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException cause) {
            // Every Java platform ships SHA-256, so reaching here means a broken runtime.
            throw new IllegalStateException(ALGORITHM + " is not available", cause);
        }
    }

    @Override
    public String toString() {
        return "********";
    }
}
