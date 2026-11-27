package ir.aut.shop.model;

/**
 * A group of related products (user stories 13 and 14).
 */
public class Category {

    private final CategoryId id;
    private String name;
    private boolean active;

    /**
     * REQUIRES: id is non-null and name is non-blank.
     * EFFECTS: Builds an active category, or throws IllegalArgumentException when the name
     * is blank (user story 13).
     *
     * @param id the identity of this category
     * @param name the name shown to customers
     */
    public Category(CategoryId id, String name) {
        if (id == null) {
            throw new IllegalArgumentException("Category id must not be null");
        }
        this.id = id;
        this.name = requireName(name);
        this.active = true;
        checkRep();
    }

    public CategoryId id() {
        return id;
    }

    public String name() {
        return name;
    }

    /**
     * Reports whether this category is shown to customers.
     *
     * @return true when the category is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * REQUIRES: name is non-blank.
     * MODIFIES: this category.
     * EFFECTS: Replaces the displayed name.
     *
     * @param name the new name
     */
    public void rename(String name) {
        this.name = requireName(name);
        checkRep();
    }

    /**
     * MODIFIES: this category.
     * EFFECTS: Hides this category from customers. Its products keep their category id.
     */
    public void deactivate() {
        this.active = false;
        checkRep();
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name must not be blank");
        }
        return name.trim();
    }

    // RI: id is non-null and name is non-blank.
    private void checkRep() {
        assert id != null : "category id is null";
        assert name != null && !name.isBlank() : "category name is blank";
    }

    @Override
    public String toString() {
        return name;
    }
}
