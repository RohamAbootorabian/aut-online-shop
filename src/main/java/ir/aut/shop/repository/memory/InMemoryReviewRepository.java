package ir.aut.shop.repository.memory;

import ir.aut.shop.model.ReviewId;
import ir.aut.shop.model.Review;
import ir.aut.shop.repository.ReviewRepository;

/**
 * Product reviews kept in memory.
 */
public class InMemoryReviewRepository extends InMemoryRepository<ReviewId, Review> implements ReviewRepository {
}
