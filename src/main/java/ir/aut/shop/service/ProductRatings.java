package ir.aut.shop.service;

import ir.aut.shop.model.ProductId;

/**
 * How well a product has been rated, to whoever only needs the number.
 *
 * <p>The catalogue sorts and filters by rating, so it needs one number per product. It has
 * no business writing a review, deleting one or reading who wrote what. Asking for this
 * interface instead of for the whole review service keeps that line visible: the catalogue
 * cannot reach past it even by accident (interface segregation), and it is written against
 * an idea rather than against {@link ReviewService} (dependency inversion).
 */
@FunctionalInterface
public interface ProductRatings {

    /**
     * REQUIRES: productId is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the product's mean star rating, or zero when nobody has rated it.
     *
     * @param productId whose score to read
     * @return the average rating, between zero and five
     */
    double averageRating(ProductId productId);
}
