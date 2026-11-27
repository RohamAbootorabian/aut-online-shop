package ir.aut.shop.service;

import ir.aut.shop.model.Category;
import ir.aut.shop.model.CategoryId;
import ir.aut.shop.model.Product;
import ir.aut.shop.model.ProductId;
import ir.aut.shop.model.SearchPhrase;
import ir.aut.shop.model.SortOption;
import ir.aut.shop.repository.CategoryRepository;
import ir.aut.shop.repository.ProductRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Browsing, searching, filtering and sorting what the shop sells
 * (user stories 8 to 14).
 *
 * <p>Every list this service returns holds active products only, which is the rule user
 * stories 8, 10 and 14 all repeat.
 */
public class CatalogService {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final ProductRatings ratings;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds the service over the given stores.
     *
     * @param products where products are kept
     * @param categories where categories are kept
     * @param ratings supplies the scores that filtering and sorting need
     */
    public CatalogService(ProductRepository products, CategoryRepository categories,
            ProductRatings ratings) {
        if (products == null || categories == null || ratings == null) {
            throw new IllegalArgumentException("CatalogService is missing a dependency");
        }
        this.products = products;
        this.categories = categories;
        this.ratings = ratings;
    }

    /**
     * REQUIRES: nothing.
     * MODIFIES: nothing.
     * EFFECTS: Returns the categories customers may browse, by name (user story 13).
     *
     * @return the active categories
     */
    public List<Category> listActiveCategories() {
        return categories.findAll().stream()
                .filter(Category::isActive)
                .sorted(Comparator.comparing(Category::name))
                .toList();
    }

    /**
     * REQUIRES: nothing.
     * MODIFIES: nothing.
     * EFFECTS: Returns every product on sale, by name (user story 8).
     *
     * @return the active products
     */
    public List<Product> listActiveProducts() {
        return products.findAll().stream()
                .filter(Product::isActive)
                .sorted(Comparator.comparing(Product::name))
                .toList();
    }

    /**
     * REQUIRES: categoryId is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the active products of that category, or throws
     * IllegalArgumentException when the category does not exist (user story 14). An empty
     * list means the category holds nothing, which the caller reports to the customer.
     *
     * @param categoryId whose products to list
     * @return the active products in that category
     */
    public List<Product> productsInCategory(CategoryId categoryId) {
        if (!categories.contains(categoryId)) {
            throw new IllegalArgumentException("No category with id " + categoryId);
        }
        return listActiveProducts().stream()
                .filter(product -> product.categoryId().equals(categoryId))
                .toList();
    }

    /**
     * REQUIRES: productId is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the product with that identity, active or not, or empty when there
     * is none (user story 9).
     *
     * @param productId the product to look up
     * @return the product, or empty
     */
    public Optional<Product> findProduct(ProductId productId) {
        return (products.findById(productId));
    }

    /**
     * REQUIRES: categoryId is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the category with that identity, or empty when there is none.
     *
     * @param categoryId the category to look up
     * @return the category, or empty
     */
    public Optional<Category> findCategory(CategoryId categoryId) {
        return (categories.findById(categoryId));
    }

    /**
     * REQUIRES: phrase is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the active products whose name, description or brand contains the
     * phrase, by name. The phrase length is capped by the type itself (user story 10).
     *
     * @param phrase what the customer typed
     * @return the matching active products
     */
    public List<Product> search(SearchPhrase phrase) {
        if (phrase == null) {
            throw new IllegalArgumentException("Search phrase must not be null");
        }
        return listActiveProducts().stream()
                .filter(product -> phrase.matches(product.name())
                        || phrase.matches(product.description())
                        || phrase.matches(product.brand()))
                .toList();
    }

    /**
     * REQUIRES: filter is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the active products that pass every condition of the filter, by
     * name (user story 11).
     *
     * @param filter the conditions the customer set
     * @return the products that pass
     */
    public List<Product> filter(ProductFilterRequest filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }
        return listActiveProducts().stream()
                .filter(product -> filter.accepts(product, ratingOf(product)))
                .toList();
    }

    /**
     * REQUIRES: products and option are non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the same products arranged in the chosen order. The input list is
     * not changed (user story 12).
     *
     * @param productsToSort the products to arrange
     * @param option the order the customer picked
     * @return a new list in that order
     */
    public List<Product> sort(List<Product> productsToSort, SortOption option) {
        if (productsToSort == null || option == null) {
            throw new IllegalArgumentException("Products and sort option must not be null");
        }
        return productsToSort.stream()
                .sorted(comparatorFor(option))
                .toList();
    }

    /**
     * REQUIRES: product is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the product's mean star rating, or zero when nobody has rated it.
     *
     * @param product whose score to read
     * @return the average rating
     */
    public double ratingOf(Product product) {
        return ratings.averageRating(product.id());
    }

    private Comparator<Product> comparatorFor(SortOption option) {
        return switch (option) {
            case CHEAPEST -> Comparator.comparing(product -> product.price().amount());
            case MOST_EXPENSIVE ->
                    Comparator.<Product, java.math.BigDecimal>comparing(
                            product -> product.price().amount()).reversed();
            case NEWEST -> Comparator.comparing(Product::createdAt).reversed();
            case MOST_POPULAR -> Comparator.comparingInt(Product::timesSold).reversed();
            case HIGHEST_RATED -> Comparator.comparingDouble(this::ratingOf).reversed();
        };
    }
}
