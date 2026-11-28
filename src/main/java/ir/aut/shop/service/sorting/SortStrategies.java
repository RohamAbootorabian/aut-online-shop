package ir.aut.shop.service.sorting;

import ir.aut.shop.model.SortOption;
import java.util.EnumMap;
import java.util.Map;

/**
 * Which strategy belongs to which option the customer picked.
 *
 * <p>This is the one place that knows the pairing, and it is filled from outside rather
 * than hard-coded, so the catalogue never learns how many orders exist or what they are
 * called. Adding a sixth order means writing one more {@link ProductSortStrategy} and
 * registering it; no class that already works is opened.
 *
 * <p>The enum itself still lists the options, because user story 12 fixes the menu the
 * customer is shown and an enum is what keeps an unknown option from ever arriving. The
 * principle being served here is that the *behaviour* is open for extension — the
 * catalogue's sorting code does not grow a branch for every new order.
 */
public class SortStrategies {

    private final Map<SortOption, ProductSortStrategy> strategies =
            new EnumMap<>(SortOption.class);

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds an empty registry. Nothing can be sorted until strategies are
     * registered.
     */
    public SortStrategies() {
    }

    /**
     * REQUIRES: option and strategy are non-null.
     * MODIFIES: this registry.
     * EFFECTS: Pairs the option with the strategy, replacing any earlier pairing, and
     * returns this registry so registrations can be chained.
     *
     * @param option the option a customer may pick
     * @param strategy how to arrange products for it
     * @return this registry
     */
    public SortStrategies register(SortOption option, ProductSortStrategy strategy) {
        if (option == null || strategy == null) {
            throw new IllegalArgumentException("A sort option must be paired with a strategy");
        }
        strategies.put(option, strategy);
        return this;
    }

    /**
     * REQUIRES: option is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the strategy registered for the option, or throws
     * IllegalStateException when none was registered. That is a wiring mistake, not
     * something a customer can cause, which is why it is a state error and not an
     * argument error.
     *
     * @param option the option a customer picked
     * @return how to arrange products for it
     */
    public ProductSortStrategy strategyFor(SortOption option) {
        if (option == null) {
            throw new IllegalArgumentException("Sort option must not be null");
        }
        ProductSortStrategy strategy = strategies.get(option);
        if (strategy == null) {
            throw new IllegalStateException("No sort strategy was registered for " + option);
        }
        return strategy;
    }

    /**
     * REQUIRES: nothing.
     * MODIFIES: nothing.
     * EFFECTS: Returns a registry holding the five orders user story 12 asks for.
     *
     * @return the shop's standard registry
     */
    public static SortStrategies standard() {
        return new SortStrategies()
                .register(SortOption.CHEAPEST, new CheapestFirst())
                .register(SortOption.MOST_EXPENSIVE, new MostExpensiveFirst())
                .register(SortOption.NEWEST, new NewestFirst())
                .register(SortOption.MOST_POPULAR, new MostPopularFirst())
                .register(SortOption.HIGHEST_RATED, new HighestRatedFirst());
    }
}
