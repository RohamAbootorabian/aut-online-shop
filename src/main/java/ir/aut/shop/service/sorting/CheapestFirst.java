package ir.aut.shop.service.sorting;

import ir.aut.shop.model.Product;
import ir.aut.shop.service.ProductRatings;
import java.util.Comparator;

/** Lowest price first. */
public class CheapestFirst implements ProductSortStrategy {

    @Override
    public Comparator<Product> comparator(ProductRatings ratings) {
        return Comparator.comparing(product -> product.price().amount());
    }
}
