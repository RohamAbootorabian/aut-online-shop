package ir.aut.shop.service.sorting;

import ir.aut.shop.model.Product;
import ir.aut.shop.service.ProductRatings;
import java.util.Comparator;

/** Most units sold first. */
public class MostPopularFirst implements ProductSortStrategy {

    @Override
    public Comparator<Product> comparator(ProductRatings ratings) {
        return Comparator.comparingInt(Product::timesSold).reversed();
    }
}
