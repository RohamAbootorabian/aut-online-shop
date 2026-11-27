package ir.aut.shop.model;

import java.util.Locale;

/**
 * What a customer typed into the product search box.
 *
 * <p>User story 10 caps the phrase at one hundred characters, so the cap belongs to the
 * type rather than to each caller.
 *
 * @param value the phrase, trimmed and lower-cased for case-insensitive matching
 */
public record SearchPhrase(String value) {

    /** Longest phrase the shop accepts, from user story 10. */
    public static final int MAX_LENGTH = 100;

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the phrase, or throws IllegalArgumentException when it is blank or
     * longer than MAX_LENGTH.
     */
    public SearchPhrase {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Search phrase must not be blank");
        }
        if (value.trim().length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Search phrase must be at most " + MAX_LENGTH + " characters");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Reports whether a piece of text contains this phrase.
     *
     * @param text the text to look in, such as a product name
     * @return true when the text contains the phrase, ignoring case
     */
    public boolean matches(String text) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
