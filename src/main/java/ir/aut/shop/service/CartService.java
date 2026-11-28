package ir.aut.shop.service;

import ir.aut.shop.model.Cart;
import ir.aut.shop.model.Product;
import ir.aut.shop.model.ProductId;
import ir.aut.shop.model.Quantity;
import ir.aut.shop.model.UserId;
import ir.aut.shop.repository.CartRepository;
import ir.aut.shop.repository.ProductRepository;

/**
 * Keeping each customer's shopping cart (user stories 15 to 19).
 *
 * <p>The stock checks live here rather than in {@link Cart}, because the cart knows what
 * the customer wants while only the catalogue knows what is on the shelf.
 */
public class CartService implements CheckoutBasket {

    private final CartRepository carts;
    private final ProductRepository products;

    /**
     * REQUIRES: both stores are non-null.
     * EFFECTS: Builds the service over the given carts and products.
     *
     * @param carts where carts are kept
     * @param products where products are kept
     */
    public CartService(CartRepository carts, ProductRepository products) {
        if (carts == null || products == null) {
            throw new IllegalArgumentException("Cart and product repositories must not be null");
        }
        this.carts = carts;
        this.products = products;
    }

    /**
     * REQUIRES: ownerId is non-null.
     * MODIFIES: the cart repository, when the customer had no cart yet.
     * EFFECTS: Returns the customer's cart, creating an empty one the first time
     * (user story 16).
     *
     * @param ownerId whose cart to fetch
     * @return that customer's cart
     */
    @Override
    public Cart cartOf(UserId ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Cart owner must not be null");
        }
        return carts.findById(ownerId).orElseGet(() -> {
            Cart fresh = new Cart(ownerId);
            carts.save(ownerId, fresh);
            return fresh;
        });
    }

    /**
     * REQUIRES: every argument is non-null.
     * MODIFIES: the customer's cart.
     * EFFECTS: Adds the quantity to the cart, or throws IllegalArgumentException when the
     * product cannot be bought or the shelf cannot cover what the cart would then hold
     * (user story 15).
     *
     * @param ownerId whose cart to add to
     * @param productId the product to add
     * @param quantity how many units to add
     */
    public void addToCart(UserId ownerId, ProductId productId, Quantity quantity) {
        Product product = requirePurchasableProduct(productId);
        Cart cart = cartOf(ownerId);
        int wantedInTotal = cart.quantityOf(productId) + quantity.value();
        requireStockCovers(product, new Quantity(wantedInTotal));
        cart.addProduct(product, quantity);
    }

    /**
     * REQUIRES: every argument is non-null.
     * MODIFIES: the customer's cart.
     * EFFECTS: Sets how many units of the product the cart holds, or throws
     * IllegalArgumentException when the product is not in the cart or the shelf cannot
     * cover the new quantity (user story 17).
     *
     * @param ownerId whose cart to change
     * @param productId the product to change
     * @param quantity the quantity to set
     */
    public void changeQuantity(UserId ownerId, ProductId productId, Quantity quantity) {
        Product product = requireKnownProduct(productId);
        requireStockCovers(product, quantity);
        cartOf(ownerId).changeQuantity(productId, quantity);
    }

    /**
     * REQUIRES: ownerId and productId are non-null.
     * MODIFIES: the customer's cart.
     * EFFECTS: Drops the product from the cart. Dropping something the cart does not hold
     * changes nothing (user story 18).
     *
     * @param ownerId whose cart to change
     * @param productId the product to drop
     */
    public void removeFromCart(UserId ownerId, ProductId productId) {
        cartOf(ownerId).removeProduct(productId);
    }

    /**
     * REQUIRES: ownerId is non-null.
     * MODIFIES: the customer's cart.
     * EFFECTS: Empties the cart, so it holds nothing and its subtotal is zero
     * (user story 19).
     *
     * @param ownerId whose cart to empty
     */
    @Override
    public void clearCart(UserId ownerId) {
        cartOf(ownerId).clear();
    }

    private void requireStockCovers(Product product, Quantity quantity) {
        if (!product.hasEnoughStock(quantity)) {
            throw new IllegalArgumentException("Only " + product.stock()
                    + " units of " + product.name() + " are in stock");
        }
    }

    private Product requirePurchasableProduct(ProductId productId) {
        Product product = requireKnownProduct(productId);
        if (!product.isPurchasable()) {
            throw new IllegalArgumentException(
                    product.name() + " cannot be bought right now");
        }
        return product;
    }

    private Product requireKnownProduct(ProductId productId) {
        return products.findById(productId).orElseThrow(
                () -> new IllegalArgumentException("No product with id " + productId));
    }
}
