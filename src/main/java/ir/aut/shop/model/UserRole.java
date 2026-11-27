package ir.aut.shop.model;

/**
 * What a signed-in user is allowed to do.
 *
 * <p>A visitor who has not signed in is not a role: they simply have no {@link User} yet.
 */
public enum UserRole {

    /** Buys products, manages their own cart, orders and reviews. */
    CUSTOMER,

    /** Manages products, stock and every order in the shop. */
    ADMIN
}
