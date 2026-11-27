package ir.aut.shop.model;

import java.util.regex.Pattern;

/**
 * An email address used to identify and reach a user.
 *
 * <p>User story 1 requires the address to have a valid format. The check here is
 * deliberately shallow: it rejects obvious nonsense without pretending to prove the
 * mailbox exists.
 *
 * @param value the address text, normalised to lower case
 */
public record Email(String value) {

    private static final Pattern VALID_FORMAT =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]{2,}$");

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the lower-cased address, or throws IllegalArgumentException when the
     * text is null or badly formatted.
     */
    public Email {
        if (value == null || !VALID_FORMAT.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Not a valid email address: " + value);
        }
        value = value.trim().toLowerCase();
    }

    @Override
    public String toString() {
        return value;
    }
}
