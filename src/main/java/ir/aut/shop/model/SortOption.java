package ir.aut.shop.model;

/**
 * The orders in which a customer may arrange a product list.
 *
 * <p>User story 12 lists exactly these five options and states that no other option may be
 * chosen, which an enum enforces for free.
 */
public enum SortOption {

    /** Lowest price first. */
    CHEAPEST,

    /** Highest price first. */
    MOST_EXPENSIVE,

    /** Most recently added first. */
    NEWEST,

    /** Most units sold first. */
    MOST_POPULAR,

    /** Highest average review rating first. */
    HIGHEST_RATED
}
