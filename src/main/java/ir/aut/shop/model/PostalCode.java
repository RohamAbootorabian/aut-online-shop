package ir.aut.shop.model;

import java.util.regex.Pattern;

/**
 * The postal code part of a delivery address (user story 7).
 *
 * @param value the ten digits of the code
 */
public record PostalCode(String value) {

    private static final Pattern VALID_FORMAT = Pattern.compile("^\\d{10}$");

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the code, or throws IllegalArgumentException when it is not ten
     * digits.
     */
    public PostalCode {
        if (value == null || !VALID_FORMAT.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Postal code must be 10 digits: " + value);
        }
        value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
