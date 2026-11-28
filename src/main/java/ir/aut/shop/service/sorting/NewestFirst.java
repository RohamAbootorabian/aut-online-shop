package ir.aut.shop.service.sorting;

import ir.aut.shop.model.Product;
import ir.aut.shop.service.ProductRatings;
import java.util.Comparator;

/** Most recently added first. */
public class NewestFirst implements ProductSortStrategy {

    @Override
    public Comparator<Product> comparator(ProductRatings ratings) {
        return Comparator.comparing(Product::createdAt).reversed();
    }
}
