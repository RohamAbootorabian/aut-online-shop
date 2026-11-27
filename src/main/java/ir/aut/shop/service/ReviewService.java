package ir.aut.shop.service;

import ir.aut.shop.model.ProductId;
import ir.aut.shop.model.Rating;
import ir.aut.shop.model.Review;
import ir.aut.shop.model.ReviewId;
import ir.aut.shop.model.UserId;
import java.time.LocalDateTime;
import ir.aut.shop.repository.ReviewRepository;
import java.util.Comparator;
import java.util.List;

/**
 * Opinions and scores customers leave on products (user stories 30 to 32).
 */
public class ReviewService {

    private final ReviewRepository reviews;
    private final OrderService orderService;

    /**
     * REQUIRES: both arguments are non-null.
     * EFFECTS: Builds the service over the given review store.
     *
     * @param reviews where reviews are kept
     * @param orderService used to check that a reviewer actually bought the product
     */
    public ReviewService(ReviewRepository reviews, OrderService orderService) {
        if (reviews == null || orderService == null) {
            throw new IllegalArgumentException("ReviewService is missing a dependency");
        }
        this.reviews = reviews;
        this.orderService = orderService;
    }

    /**
     * REQUIRES: every argument is non-null; text is non-blank.
     * MODIFIES: the review store.
     * EFFECTS: Publishes the review and returns it, or throws IllegalArgumentException
     * when the customer has not bought the product or has already reviewed it. User
     * stories 30 and 31 both require the reviewer to be a buyer.
     *
     * @param authorId who is reviewing
     * @param productId the product being reviewed
     * @param text the opinion
     * @param rating the star rating, one to five
     * @return the published review
     */
    public Review addReview(UserId authorId, ProductId productId, String text, Rating rating) {
        if (authorId == null || productId == null) {
            throw new IllegalArgumentException("Review needs an author and a product");
        }
        if (!orderService.hasPurchased(authorId, productId)) {
            throw new IllegalArgumentException("Only a buyer of this product may review it");
        }
        if (hasReviewed(authorId, productId)) {
            throw new IllegalArgumentException("You have already reviewed this product");
        }
        Review review = new Review(ReviewId.newId(), productId, authorId, text, rating,
                LocalDateTime.now());
        reviews.save(review.id(), review);
        return review;
    }

    /**
     * REQUIRES: productId is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the product's reviews, newest first (user story 32).
     *
     * @param productId whose reviews to list
     * @return the published reviews of that product
     */
    public List<Review> reviewsOf(ProductId productId) {
        return reviews.findAll().stream()
                .filter(review -> review.productId().equals(productId))
                .sorted(Comparator.comparing(Review::createdAt).reversed())
                .toList();
    }

    /**
     * REQUIRES: productId is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the mean star rating of the product, or zero when nobody has rated
     * it yet (user stories 9, 11 and 12).
     *
     * @param productId whose score to work out
     * @return the average rating, between zero and five
     */
    public double averageRating(ProductId productId) {
        return reviews.findAll().stream()
                .filter(review -> review.productId().equals(productId))
                .mapToInt(review -> review.rating().stars())
                .average()
                .orElse(0.0);
    }

    /**
     * REQUIRES: productId is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns how many reviews the product has.
     *
     * @param productId whose reviews to count
     * @return the number of reviews
     */
    public int reviewCount(ProductId productId) {
        return reviewsOf(productId).size();
    }

    private boolean hasReviewed(UserId authorId, ProductId productId) {
        return reviews.findAll().stream()
                .anyMatch(review -> review.authorId().equals(authorId)
                        && review.productId().equals(productId));
    }
}
