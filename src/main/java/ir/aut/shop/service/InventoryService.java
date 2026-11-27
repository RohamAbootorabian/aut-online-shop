package ir.aut.shop.service;

import ir.aut.shop.model.Product;
import ir.aut.shop.model.ProductId;
import ir.aut.shop.model.Quantity;
import ir.aut.shop.model.StockChange;
import ir.aut.shop.model.User;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Looking at and adjusting what the warehouse holds (user stories 28 and 29).
 *
 * <p>Both stories say "only an admin", so every operation here checks the actor's role.
 * That is a rule of the business, not of the screen, which is why it lives in this layer.
 */
public class InventoryService {

    private final Map<ProductId, Product> products;
    private final List<StockChange> changeLog;

    /**
     * REQUIRES: both arguments are non-null.
     * EFFECTS: Builds the service over the given product store and log.
     *
     * @param products where products are kept, keyed by identity
     * @param changeLog where stock adjustments are recorded
     */
    public InventoryService(Map<ProductId, Product> products, List<StockChange> changeLog) {
        if (products == null || changeLog == null) {
            throw new IllegalArgumentException("InventoryService is missing a dependency");
        }
        this.products = products;
        this.changeLog = changeLog;
    }

    /**
     * REQUIRES: actor is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns every product with its current stock, by name, or throws
     * IllegalArgumentException when the actor is not an admin (user story 28).
     *
     * @param actor who is asking
     * @return every product, active or not
     */
    public List<Product> viewStock(User actor) {
        requireAdmin(actor);
        return products.values().stream()
                .sorted(Comparator.comparing(Product::name))
                .toList();
    }

    /**
     * REQUIRES: actor, productId and reason are non-null; reason is non-blank; delta is
     * not zero.
     * MODIFIES: the named product and the change log.
     * EFFECTS: Adds units when the delta is positive and removes them when it is negative,
     * then records the change. Throws IllegalArgumentException when the actor is not an
     * admin, the product is unknown, or the removal would push the stock below zero, which
     * user story 29 forbids outright.
     *
     * @param actor the admin making the change
     * @param productId whose stock to change
     * @param delta how many units to add, or remove when negative
     * @param reason why the stock is being changed
     */
    public void changeStock(User actor, ProductId productId, int delta, String reason) {
        requireAdmin(actor);
        Product product = requireProduct(productId);
        if (delta == 0) {
            throw new IllegalArgumentException("Stock change must not be zero");
        }

        if (delta > 0) {
            product.increaseStock(delta);
        } else {
            product.reduceStock(new Quantity(-delta));
        }
        changeLog.add(new StockChange(productId, delta, reason, actor.id(),
                LocalDateTime.now()));
    }

    /**
     * REQUIRES: actor and productId are non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the recorded changes for that product, newest first, or throws
     * IllegalArgumentException when the actor is not an admin.
     *
     * @param actor who is asking
     * @param productId whose history to read
     * @return the recorded stock changes
     */
    public List<StockChange> changeLogOf(User actor, ProductId productId) {
        requireAdmin(actor);
        return changeLog.stream()
                .filter(change -> change.productId().equals(productId))
                .sorted(Comparator.comparing(StockChange::changedAt).reversed())
                .toList();
    }

    private Product requireProduct(ProductId productId) {
        Product product = products.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("No product with id " + productId);
        }
        return product;
    }

    private static void requireAdmin(User actor) {
        if (actor == null || !actor.isAdmin()) {
            throw new IllegalArgumentException("Only an admin may manage stock");
        }
    }
}
