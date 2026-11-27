package ir.aut.shop.service;

import ir.aut.shop.model.Category;
import ir.aut.shop.model.CategoryId;
import ir.aut.shop.model.Money;
import ir.aut.shop.model.Product;
import ir.aut.shop.model.ProductId;
import ir.aut.shop.model.StockCount;
import ir.aut.shop.model.User;
import java.time.LocalDateTime;
import ir.aut.shop.repository.CategoryRepository;
import ir.aut.shop.repository.ProductRepository;

/**
 * Creating, editing and switching off products (user stories 33 to 35).
 *
 * <p>All three stories say "only an admin", so every operation checks the actor's role.
 */
public class ProductAdminService {

    private final AccessPolicy access;
    private final ProductRepository products;
    private final CategoryRepository categories;

    /**
     * REQUIRES: both stores are non-null.
     * EFFECTS: Builds the service over the given stores.
     *
     * @param products where products are kept
     * @param categories where categories are kept
     * @param access decides who may act
     */
    public ProductAdminService(ProductRepository products,
            CategoryRepository categories, AccessPolicy access) {
        if (products == null || categories == null || access == null) {
            throw new IllegalArgumentException("ProductAdminService is missing a dependency");
        }
        this.products = products;
        this.categories = categories;
        this.access = access;
    }

    /**
     * REQUIRES: every argument is non-null; name and brand are non-blank.
     * MODIFIES: the product store.
     * EFFECTS: Puts a new active product on sale and returns it, or throws
     * IllegalArgumentException when the actor is not an admin or the category does not
     * exist. The name, price and stock rules of user story 33 are enforced by the types
     * themselves.
     *
     * @param actor the admin creating the product
     * @param name the name shown to customers
     * @param description the longer description
     * @param price the price of one unit
     * @param stock how many units to put on the shelf
     * @param categoryId which category it belongs to
     * @param brand the manufacturer
     * @return the product that was created
     */
    public Product createProduct(User actor, String name, String description, Money price,
            StockCount stock, CategoryId categoryId, String brand) {
        access.requireAdmin(actor, "manage products");
        requireKnownCategory(categoryId);
        Product product = new Product(ProductId.newId(), name, description, price, stock,
                categoryId, brand, LocalDateTime.now());
        products.save(product.id(), product);
        return product;
    }

    /**
     * REQUIRES: every argument is non-null; name and brand are non-blank.
     * MODIFIES: the named product.
     * EFFECTS: Replaces the product's details, or throws IllegalArgumentException when the
     * actor is not an admin, the product is unknown, or the category does not exist
     * (user story 34). The stock is not touched here; that is
     * {@link InventoryService}'s job.
     *
     * @param actor the admin making the change
     * @param productId which product to change
     * @param name the new name
     * @param description the new description
     * @param price the new unit price
     * @param categoryId the category to move it to
     * @param brand the new brand
     */
    public void updateProduct(User actor, ProductId productId, String name, String description,
            Money price, CategoryId categoryId, String brand) {
        access.requireAdmin(actor, "manage products");
        requireKnownCategory(categoryId);
        Product product = requireProduct(productId);
        product.rename(name);
        product.changeDescription(description);
        product.changePrice(price);
        product.moveToCategory(categoryId);
        product.changeBrand(brand);
    }

    /**
     * REQUIRES: actor and productId are non-null.
     * MODIFIES: the named product.
     * EFFECTS: Hides the product so customers can no longer buy it, or throws
     * IllegalArgumentException when the actor is not an admin or the product is unknown.
     * Past orders keep their copy of the product and are untouched (user story 35).
     *
     * @param actor the admin switching it off
     * @param productId which product to switch off
     */
    public void deactivateProduct(User actor, ProductId productId) {
        access.requireAdmin(actor, "manage products");
        requireProduct(productId).deactivate();
    }

    /**
     * REQUIRES: actor and productId are non-null.
     * MODIFIES: the named product.
     * EFFECTS: Puts the product back on sale, or throws IllegalArgumentException when the
     * actor is not an admin or the product is unknown.
     *
     * @param actor the admin switching it back on
     * @param productId which product to switch back on
     */
    public void activateProduct(User actor, ProductId productId) {
        access.requireAdmin(actor, "manage products");
        requireProduct(productId).activate();
    }

    /**
     * REQUIRES: actor is non-null; name is non-blank.
     * MODIFIES: the category store.
     * EFFECTS: Adds a category and returns it, or throws IllegalArgumentException when the
     * actor is not an admin.
     *
     * @param actor the admin creating the category
     * @param name the name shown to customers
     * @return the category that was created
     */
    public Category createCategory(User actor, String name) {
        access.requireAdmin(actor, "manage products");
        Category category = new Category(CategoryId.newId(), name);
        categories.save(category.id(), category);
        return category;
    }

    private void requireKnownCategory(CategoryId categoryId) {
        if (!categories.contains(categoryId)) {
            throw new IllegalArgumentException("No category with id " + categoryId);
        }
    }

    private Product requireProduct(ProductId productId) {
        return products.findById(productId).orElseThrow(
                () -> new IllegalArgumentException("No product with id " + productId));
    }

}
