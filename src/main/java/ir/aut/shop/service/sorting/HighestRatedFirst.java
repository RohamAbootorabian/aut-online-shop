package ir.aut.shop.service.sorting;

import ir.aut.shop.model.Product;
import ir.aut.shop.service.ProductRatings;
import java.util.Comparator;

/** Highest average review rating first. */
public class HighestRatedFirst implements ProductSortStrategy {

    @Override
    public Comparator<Product> comparator(ProductRatings ratings) {
        return Comparator.comparingDouble(
                (Product product) -> ratings.averageRating(product.id())).reversed();
    }
}
