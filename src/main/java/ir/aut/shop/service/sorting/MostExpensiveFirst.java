package ir.aut.shop.service.sorting;

import ir.aut.shop.model.Product;
import ir.aut.shop.service.ProductRatings;
import java.math.BigDecimal;
import java.util.Comparator;

/** Highest price first. */
public class MostExpensiveFirst implements ProductSortStrategy {

    @Override
    public Comparator<Product> comparator(ProductRatings ratings) {
        return Comparator.<Product, BigDecimal>comparing(product -> product.price().amount())
                .reversed();
    }
}
