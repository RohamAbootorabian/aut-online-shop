package ir.aut.shop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Something the shop sells.
 *
 * <p>Price and stock change often; the product itself stays the same thing, which is why
 * this is an entity.
 *
 * <p>Reviews are deliberately not held here. Keeping them in {@link Review} means reading
 * a product does not drag in every opinion ever written about it; the average score is
 * worked out by the review service instead.
 */
public class Product {

    private final ProductId id;
    private final LocalDateTime createdAt;
    private final List<String> imageUrls;
    private String name;
    private String description;
    private String brand;
    private Money price;
    private StockCount stock;
    private CategoryId categoryId;
    private boolean active;
    private int timesSold;

    /**
     * REQUIRES: every argument is non-null; name and brand are non-blank.
     * EFFECTS: Builds an active product with no images and nothing sold yet, or throws
     * IllegalArgumentException when an argument is missing or blank.
     *
     * @param id the identity of this product
     * @param name the name shown to customers, non-blank (user story 33)
     * @param description the longer description shown on the product page
     * @param price the price of one unit, non-negative (user story 33)
     * @param stock how many units are on the shelf, non-negative (user story 33)
     * @param categoryId the category this product belongs to (user story 33)
     * @param brand the manufacturer, used by the brand filter (user story 11)
     * @param createdAt when the product was added, used by the newest-first sort
     */
    public Product(ProductId id, String name, String description, Money price, StockCount stock,
            CategoryId categoryId, String brand, LocalDateTime createdAt) {
        if (id == null || price == null || stock == null || categoryId == null
                || createdAt == null) {
            throw new IllegalArgumentException("Product is missing a required field");
        }
        this.id = id;
        this.name = requireText(name, "name");
        this.description = description == null ? "" : description.trim();
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.brand = requireText(brand, "brand");
        this.createdAt = createdAt;
        this.imageUrls = new ArrayList<>();
        this.active = true;
        this.timesSold = 0;
        checkRep();
    }

    public ProductId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Money price() {
        return price;
    }

    public StockCount stock() {
        return stock;
    }

    public CategoryId categoryId() {
        return categoryId;
    }

    public String brand() {
        return brand;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    /**
     * Returns how many units have been sold, which drives the most-popular sort.
     *
     * @return the number of units sold so far
     */
    public int timesSold() {
        return timesSold;
    }

    /**
     * Returns the pictures of this product.
     *
     * @return an unmodifiable copy; later changes to the product leave it alone
     */
    public List<String> imageUrls() {
        return List.copyOf(imageUrls);
    }

    /**
     * Reports whether customers may see this product (user stories 8 and 35).
     *
     * @return true when the product is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Reports whether a customer may buy this product right now.
     *
     * @return true when the product is active and at least one unit is on the shelf
     */
    public boolean isPurchasable() {
        return active && !stock.isEmpty();
    }

    /**
     * Reports whether the shelf can cover a wanted quantity (user stories 15 and 17).
     *
     * @param quantity how many units the customer wants
     * @return true when the stock is at least that quantity
     */
    public boolean hasEnoughStock(Quantity quantity) {
        return stock.canCover(quantity);
    }

    /**
     * REQUIRES: name is non-blank.
     * MODIFIES: this product.
     * EFFECTS: Replaces the displayed name (user story 34).
     *
     * @param name the new name
     */
    public void rename(String name) {
        this.name = requireText(name, "name");
        checkRep();
    }

    /**
     * MODIFIES: this product.
     * EFFECTS: Replaces the description; a null description becomes empty text.
     *
     * @param description the new description
     */
    public void changeDescription(String description) {
        this.description = description == null ? "" : description.trim();
        checkRep();
    }

    /**
     * REQUIRES: price is non-null.
     * MODIFIES: this product.
     * EFFECTS: Replaces the unit price. Carts and orders keep the price they captured, so
     * this never rewrites a total that was already worked out.
     *
     * @param price the new unit price
     */
    public void changePrice(Money price) {
        if (price == null) {
            throw new IllegalArgumentException("Product price must not be null");
        }
        this.price = price;
        checkRep();
    }

    /**
     * REQUIRES: brand is non-blank.
     * MODIFIES: this product.
     * EFFECTS: Replaces the manufacturer.
     *
     * @param brand the new brand
     */
    public void changeBrand(String brand) {
        this.brand = requireText(brand, "brand");
        checkRep();
    }

    /**
     * REQUIRES: categoryId is non-null.
     * MODIFIES: this product.
     * EFFECTS: Moves the product to another category (user story 34).
     *
     * @param categoryId the category to move into
     */
    public void moveToCategory(CategoryId categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Product category must not be null");
        }
        this.categoryId = categoryId;
        checkRep();
    }

    /**
     * REQUIRES: url is non-blank.
     * MODIFIES: this product.
     * EFFECTS: Adds one picture to the product page (user story 9).
     *
     * @param url where the picture lives
     */
    public void addImage(String url) {
        imageUrls.add(requireText(url, "image url"));
        checkRep();
    }

    /**
     * REQUIRES: quantity is non-null and the shelf can cover it.
     * MODIFIES: this product.
     * EFFECTS: Takes the quantity off the shelf, or throws IllegalArgumentException when
     * the stock is too low. The stock never goes below zero (user story 29).
     *
     * @param quantity how many units to take off the shelf
     */
    public void reduceStock(Quantity quantity) {
        this.stock = stock.decreaseBy(quantity);
        checkRep();
    }

    /**
     * REQUIRES: amount is zero or more.
     * MODIFIES: this product.
     * EFFECTS: Puts units back on the shelf (user story 29).
     *
     * @param amount how many units to add
     */
    public void increaseStock(int amount) {
        this.stock = stock.increaseBy(amount);
        checkRep();
    }

    /**
     * REQUIRES: quantity is non-null.
     * MODIFIES: this product.
     * EFFECTS: Counts the quantity towards how often this product has sold, which is what
     * the most-popular sort reads (user story 12).
     *
     * @param quantity how many units were sold
     */
    public void recordSale(Quantity quantity) {
        this.timesSold += quantity.value();
        checkRep();
    }

    /**
     * MODIFIES: this product.
     * EFFECTS: Hides the product from customers so it can no longer be bought. Orders that
     * already contain it are untouched (user story 35).
     */
    public void deactivate() {
        this.active = false;
        checkRep();
    }

    /**
     * MODIFIES: this product.
     * EFFECTS: Shows the product to customers again.
     */
    public void activate() {
        this.active = true;
        checkRep();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Product " + field + " must not be blank");
        }
        return value.trim();
    }

    // RI: id, price, stock and categoryId are non-null; name and brand are non-blank;
    //     timesSold is never negative; no image url is blank.
    // Price and stock cannot be negative because Money and StockCount refuse to hold a
    // negative value, so that rule is not repeated here.
    private void checkRep() {
        assert id != null : "product id is null";
        assert price != null : "product price is null";
        assert stock != null : "product stock is null";
        assert categoryId != null : "product category is null";
        assert name != null && !name.isBlank() : "product name is blank";
        assert brand != null && !brand.isBlank() : "product brand is blank";
        assert timesSold >= 0 : "times sold is negative";
        assert imageUrls.stream().noneMatch(url -> url == null || url.isBlank())
                : "product has a blank image url";
    }

    @Override
    public String toString() {
        return name + " (" + brand + ") - " + price;
    }
}
