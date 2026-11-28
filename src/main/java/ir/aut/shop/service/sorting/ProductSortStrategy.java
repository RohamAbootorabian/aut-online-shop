package ir.aut.shop.service.sorting;

import ir.aut.shop.model.Product;
import ir.aut.shop.service.ProductRatings;
import java.util.Comparator;

/**
 * One way of arranging a list of products.
 *
 * <p>Sorting used to be a <code>switch</code> inside the catalogue, which meant that the
 * sixth order a shop ever wanted could only be had by opening a finished class and editing
 * it. Here each order is its own small class instead: a new one is a new file, and nothing
 * already written and tested is touched. That is the open–closed principle stated as code
 * rather than as a comment.
 *
 * <p>The ratings are passed in rather than held, because only one strategy needs them and
 * the others must not be made to carry a dependency they have no use for.
 */
@FunctionalInterface
public interface ProductSortStrategy {

    /**
     * REQUIRES: ratings is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the comparator that puts products in this order.
     *
     * @param ratings how well each product has been rated
     * @return the order to arrange products in
     */
    Comparator<Product> comparator(ProductRatings ratings);
}
