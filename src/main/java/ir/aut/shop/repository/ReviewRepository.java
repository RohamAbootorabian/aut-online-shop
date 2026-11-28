package ir.aut.shop.repository;

import ir.aut.shop.model.ReviewId;
import ir.aut.shop.model.Review;

/**
 * Where product reviews are kept.
 */
public interface ReviewRepository extends Repository<ReviewId, Review> {
}
