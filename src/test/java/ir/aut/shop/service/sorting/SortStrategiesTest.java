package ir.aut.shop.service.sorting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ir.aut.shop.model.CategoryId;
import ir.aut.shop.model.Money;
import ir.aut.shop.model.Product;
import ir.aut.shop.model.ProductId;
import ir.aut.shop.model.SortOption;
import ir.aut.shop.model.StockCount;
import ir.aut.shop.service.ProductRatings;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * That sorting is open for extension and closed for modification.
 *
 * <p>The last test is the one that matters: it adds a sixth order the shop has never seen
 * and arranges products by it without opening a single class that already works.
 */
class SortStrategiesTest {

    private static final CategoryId CATEGORY = CategoryId.newId();
    private static final ProductRatings NO_RATINGS = productId -> 0.0;

    private static Product product(String name, String price) {
        return new Product(ProductId.newId(), name, "a description", Money.of(price),
                new StockCount(5), CATEGORY, "a brand", LocalDateTime.now());
    }

    private static List<String> namesOf(List<Product> products, Comparator<Product> order) {
        return products.stream().sorted(order).map(Product::name).toList();
    }

    @Test
    @DisplayName("Story 12: the cheapest order puts the lowest price first")
    void cheapestFirst() {
        List<Product> products =
                List.of(product("Laptop", "78000000"), product("Book", "950000"));

        assertEquals(List.of("Book", "Laptop"),
                namesOf(products, new CheapestFirst().comparator(NO_RATINGS)));
    }

    @Test
    @DisplayName("Story 12: the dearest order is the exact reverse")
    void mostExpensiveFirst() {
        List<Product> products =
                List.of(product("Book", "950000"), product("Laptop", "78000000"));

        assertEquals(List.of("Laptop", "Book"),
                namesOf(products, new MostExpensiveFirst().comparator(NO_RATINGS)));
    }

    @Test
    @DisplayName("The rated order asks the ratings it was handed, not a review service")
    void highestRatedUsesTheRatingsGiven() {
        Product good = product("Good", "1000");
        Product poor = product("Poor", "1000");
        ProductRatings ratings = productId -> productId.equals(good.id()) ? 4.8 : 1.2;

        assertEquals(List.of("Good", "Poor"),
                namesOf(List.of(poor, good), new HighestRatedFirst().comparator(ratings)));
    }

    @Test
    @DisplayName("The standard registry answers every option user story 12 lists")
    void standardRegistryIsComplete() {
        SortStrategies registry = SortStrategies.standard();

        for (SortOption option : SortOption.values()) {
            assertEquals(true, registry.strategyFor(option) != null,
                    "no strategy registered for " + option);
        }
    }

    @Test
    @DisplayName("An option nobody registered is a wiring mistake, reported as one")
    void unregisteredOptionIsAStateError() {
        SortStrategies empty = new SortStrategies();

        assertThrows(IllegalStateException.class,
                () -> empty.strategyFor(SortOption.NEWEST));
    }

    @Test
    @DisplayName("A sixth order can be added without opening any class that already works")
    void aNewOrderNeedsNoExistingClassToChange() {
        // Arrange products by name, which the shop has never offered.
        ProductSortStrategy byName = unused -> Comparator.comparing(Product::name);
        SortStrategies registry =
                SortStrategies.standard().register(SortOption.NEWEST, byName);

        List<Product> products =
                List.of(product("Zebra", "1000"), product("Apple", "2000"));

        assertEquals(List.of("Apple", "Zebra"), namesOf(products,
                registry.strategyFor(SortOption.NEWEST).comparator(NO_RATINGS)));
    }
}
