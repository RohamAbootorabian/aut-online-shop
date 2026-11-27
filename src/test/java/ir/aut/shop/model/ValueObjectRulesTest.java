package ir.aut.shop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks that the value objects refuse the states the user stories forbid.
 *
 * <p>Each test names the story whose constraint it stands for, so a broken rule points
 * straight back at the requirement it came from.
 */
class ValueObjectRulesTest {

    @Test
    @DisplayName("Story 8 and 33: a price is never negative")
    void moneyRefusesNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("-1"));
    }

    @Test
    @DisplayName("Money arithmetic produces new values and leaves the operands alone")
    void moneyArithmeticIsAProducer() {
        Money ten = Money.of("10.00");
        Money three = Money.of("3.00");

        assertEquals(Money.of("13.00"), ten.plus(three));
        assertEquals(Money.of("7.00"), ten.minus(three));
        assertEquals(Money.of("30.00"), ten.times(3));
        assertEquals(Money.of("10.00"), ten, "the original amount changed");
    }

    @Test
    @DisplayName("Subtracting more than there is would make a negative amount")
    void moneyRefusesNegativeResults() {
        assertThrows(IllegalArgumentException.class,
                () -> Money.of("3.00").minus(Money.of("10.00")));
    }

    @Test
    @DisplayName("Story 17: a wanted quantity is at least one")
    void quantityMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new Quantity(0));
        assertThrows(IllegalArgumentException.class, () -> new Quantity(-2));
        assertEquals(3, new Quantity(3).value());
    }

    @Test
    @DisplayName("Story 29: stock may reach zero but never go below it")
    void stockMayBeZeroButNotNegative() {
        assertEquals(0, new StockCount(0).value());
        assertThrows(IllegalArgumentException.class, () -> new StockCount(-1));
        assertThrows(IllegalArgumentException.class,
                () -> new StockCount(2).decreaseBy(new Quantity(3)));
    }

    @Test
    @DisplayName("Story 31: a rating is between one and five")
    void ratingStaysInRange() {
        assertThrows(IllegalArgumentException.class, () -> new Rating(0));
        assertThrows(IllegalArgumentException.class, () -> new Rating(6));
        assertEquals(5, new Rating(5).stars());
    }

    @Test
    @DisplayName("Story 10: a search phrase is at most one hundred characters")
    void searchPhraseIsCapped() {
        String tooLong = "a".repeat(SearchPhrase.MAX_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, () -> new SearchPhrase(tooLong));
        assertThrows(IllegalArgumentException.class, () -> new SearchPhrase("   "));
    }

    @Test
    @DisplayName("Story 10: searching ignores case")
    void searchPhraseMatchesIgnoringCase() {
        SearchPhrase phrase = new SearchPhrase("LapTop");

        assertTrue(phrase.matches("A light laptop"));
        assertFalse(phrase.matches("headphones"));
    }

    @Test
    @DisplayName("Story 1: an email must look like an email")
    void emailFormatIsChecked() {
        assertThrows(IllegalArgumentException.class, () -> new Email("not-an-email"));
        assertEquals("ali@test.com", new Email("  Ali@Test.com ").value());
    }

    @Test
    @DisplayName("Story 1 and 5: a password is stored hashed and never shown")
    void passwordIsHashedAndHidden() {
        PasswordHash hash = PasswordHash.of("secret123");

        assertTrue(hash.matches("secret123"));
        assertFalse(hash.matches("wrong"));
        assertFalse(hash.value().contains("secret123"), "raw password leaked into the hash");
        assertEquals("********", hash.toString(), "password leaked through toString");
        assertThrows(IllegalArgumentException.class, () -> PasswordHash.of("short"));
    }
}
