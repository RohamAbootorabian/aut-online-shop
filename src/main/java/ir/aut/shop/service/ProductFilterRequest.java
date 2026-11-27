package ir.aut.shop.service;

import ir.aut.shop.model.CategoryId;
import ir.aut.shop.model.Money;
import ir.aut.shop.model.Product;
import ir.aut.shop.model.Rating;

/**
 * The conditions a customer narrowed the product list with (user story 11).
 *
 * <p>Every field is optional; a null field simply does not narrow anything. Gathering them
 * into one object keeps the filtering operation down to a single parameter instead of six.
 *
 * @param categoryId keep only products in this category, or null for any
 * @param minPrice keep only products at or above this price, or null for any
 * @param maxPrice keep only products at or below this price, or null for any
 * @param brand keep only products of this brand, or null for any
 * @param minRating keep only products rated at least this high, or null for any
 * @param inStockOnly keep only products with units on the shelf
 */
public record ProductFilterRequest(CategoryId categoryId, Money minPrice, Money maxPrice,
        String brand, Rating minRating, boolean inStockOnly) {

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the filter, or throws IllegalArgumentException when the lowest price
     * is above the highest one, which user story 11 forbids.
     */
    public ProductFilterRequest {
        if (minPrice != null && maxPrice != null && maxPrice.isLessThan(minPrice)) {
            throw new IllegalArgumentException(
                    "Lowest price " + minPrice + " is above highest price " + maxPrice);
        }
        if (brand != null && brand.isBlank()) {
            brand = null;
        }
    }

    /**
     * Builds a filter that narrows nothing.
     *
     * @return a filter every active product passes
     */
    public static ProductFilterRequest none() {
        return new ProductFilterRequest(null, null, null, null, null, false);
    }

    /**
     * Reports whether a product passes every condition that was set.
     *
     * @param product the product to test
     * @param averageRating that product's mean star rating
     * @return true when the product passes
     */
    public boolean accepts(Product product, double averageRating) {
        if (categoryId != null && !product.categoryId().equals(categoryId)) {
            return false;
        }
        if (minPrice != null && product.price().isLessThan(minPrice)) {
            return false;
        }
        if (maxPrice != null && product.price().isGreaterThan(maxPrice)) {
            return false;
        }
        if (brand != null && !product.brand().equalsIgnoreCase(brand)) {
            return false;
        }
        if (minRating != null && averageRating < minRating.stars()) {
            return false;
        }
        return !inStockOnly || product.isPurchasable();
    }
}
