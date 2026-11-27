package ir.aut.shop.model;

/**
 * A star rating a customer gives to a product.
 *
 * <p>User story 31 fixes the range at one to five stars and states that a rating outside
 * that range must not be recorded. Enforcing it here makes an out-of-range rating
 * impossible to build anywhere in the program.
 *
 * @param stars the number of stars, from one to five
 */
public record Rating(int stars) {

    private static final int MIN_STARS = 1;
    private static final int MAX_STARS = 5;

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the rating, or throws IllegalArgumentException when the star count
     * is outside one to five.
     */
    public Rating {
        if (stars < MIN_STARS || stars > MAX_STARS) {
            throw new IllegalArgumentException(
                    "Rating must be between " + MIN_STARS + " and " + MAX_STARS + ": " + stars);
        }
    }

    @Override
    public String toString() {
        return stars + "/" + MAX_STARS;
    }
}
