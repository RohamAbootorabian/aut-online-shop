package ir.aut.shop.model;

import java.time.LocalDateTime;

/**
 * What one customer wrote and scored about one product (user stories 30 and 31).
 *
 * <p>Immutable: a published review is a record of an opinion at a moment in time.
 *
 * @param id the identity of this review
 * @param productId the reviewed product
 * @param authorId the customer who wrote it
 * @param text the opinion, non-blank and no longer than MAX_TEXT_LENGTH
 * @param rating the star rating given alongside the text
 * @param createdAt when the review was written
 */
public record Review(ReviewId id, ProductId productId, UserId authorId, String text,
        Rating rating, LocalDateTime createdAt) {

    /** Longest review the shop accepts; user story 30 asks for a bounded length. */
    public static final int MAX_TEXT_LENGTH = 1000;

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the review, or throws IllegalArgumentException when a field is
     * missing, the text is blank, or the text is longer than MAX_TEXT_LENGTH.
     */
    public Review {
        if (id == null || productId == null || authorId == null || rating == null
                || createdAt == null) {
            throw new IllegalArgumentException("Review is missing a required field");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Review text must not be blank");
        }
        if (text.trim().length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                    "Review text must be at most " + MAX_TEXT_LENGTH + " characters");
        }
        text = text.trim();
    }
}
