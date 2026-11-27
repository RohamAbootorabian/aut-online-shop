package ir.aut.shop.model;

import java.util.regex.Pattern;

/**
 * A mobile number used to identify and reach a user.
 *
 * @param value the digits of the number
 */
public record PhoneNumber(String value) {

    private static final Pattern VALID_FORMAT = Pattern.compile("^0\\d{10}$");

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the number, or throws IllegalArgumentException when it is not eleven
     * digits starting with zero.
     */
    public PhoneNumber {
        if (value == null || !VALID_FORMAT.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Not a valid phone number: " + value);
        }
        value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
