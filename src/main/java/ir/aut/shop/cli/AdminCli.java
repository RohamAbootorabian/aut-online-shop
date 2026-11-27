package ir.aut.shop.cli;

import ir.aut.shop.model.Category;
import ir.aut.shop.model.Money;
import ir.aut.shop.model.Order;
import ir.aut.shop.model.OrderStatus;
import ir.aut.shop.model.Product;
import ir.aut.shop.model.StockChange;
import ir.aut.shop.model.StockCount;
import ir.aut.shop.model.User;
import ir.aut.shop.service.CatalogService;
import ir.aut.shop.service.InventoryService;
import ir.aut.shop.service.OrderService;
import ir.aut.shop.service.ProductAdminService;
import java.util.List;
import java.util.Optional;

/**
 * The shop manager's screens: stock, products and every order
 * (user stories 28, 29 and 33 to 36).
 *
 * <p>This screen only hides the menu from customers. The rule that only an admin may do
 * these things is enforced in the services, so it holds however the operation is reached.
 */
public class AdminCli {

    private static final int VIEW_STOCK = 1;
    private static final int CHANGE_STOCK = 2;
    private static final int CREATE_PRODUCT = 3;
    private static final int EDIT_PRODUCT = 4;
    private static final int DEACTIVATE_PRODUCT = 5;
    private static final int CREATE_CATEGORY = 6;
    private static final int ALL_ORDERS = 7;
    private static final int ORDERS_BY_STATUS = 8;
    private static final int BACK = 0;

    private final ConsoleIo io;
    private final Session session;
    private final InventoryService inventoryService;
    private final ProductAdminService productAdminService;
    private final CatalogService catalogService;
    private final OrderService orderService;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds the screen over the given console, session and services.
     *
     * @param io where questions are asked and answers shown
     * @param session who is signed in
     * @param inventoryService stock reading and adjusting
     * @param productAdminService product and category management
     * @param catalogService supplies the lists the admin picks from
     * @param orderService supplies every order in the shop
     */
    public AdminCli(ConsoleIo io, Session session, InventoryService inventoryService,
            ProductAdminService productAdminService, CatalogService catalogService,
            OrderService orderService) {
        this.io = io;
        this.session = session;
        this.inventoryService = inventoryService;
        this.productAdminService = productAdminService;
        this.catalogService = catalogService;
        this.orderService = orderService;
    }

    /**
     * MODIFIES: products, categories and stock.
     * EFFECTS: Runs the management menu until the admin goes back.
     */
    public void run() {
        while (true) {
            io.printTitle("Manage the shop");
            io.print("  1) View stock");
            io.print("  2) Change stock");
            io.print("  3) Add a product");
            io.print("  4) Edit a product");
            io.print("  5) Switch a product off");
            io.print("  6) Add a category");
            io.print("  7) All orders");
            io.print("  8) Orders by status");
            io.print("  0) Back");

            int choice = io.readInt("Choose", BACK, ORDERS_BY_STATUS);
            try {
                switch (choice) {
                    case VIEW_STOCK -> viewStock();
                    case CHANGE_STOCK -> changeStock();
                    case CREATE_PRODUCT -> createProduct();
                    case EDIT_PRODUCT -> editProduct();
                    case DEACTIVATE_PRODUCT -> deactivateProduct();
                    case CREATE_CATEGORY -> createCategory();
                    case ALL_ORDERS -> showOrders(orderService.allOrders(admin()));
                    case ORDERS_BY_STATUS -> showOrdersByStatus();
                    default -> {
                        return;
                    }
                }
            } catch (IllegalArgumentException | IllegalStateException problem) {
                io.printError(problem.getMessage());
            }
        }
    }

    private void viewStock() {
        List<Product> products = inventoryService.viewStock(admin());
        io.printTitle("Stock");
        io.printNumberedList(products.stream()
                .map(product -> product.name() + "  stock " + product.stock()
                        + (product.isActive() ? "" : "  [switched off]"))
                .toList(), "The shop has no products yet.");
    }

    private void changeStock() {
        Optional<Product> chosen = chooseProduct("Which product", allProducts());
        if (chosen.isEmpty()) {
            return;
        }
        Product product = chosen.get();
        io.print("Current stock: " + product.stock());
        int delta = Integer.parseInt(
                io.readLine("How many units to add, or a negative number to remove"));
        String reason = io.readLine("Reason");

        inventoryService.changeStock(admin(), product.id(), delta, reason);
        io.printSuccess(product.name() + " now has " + product.stock() + " in stock.");
        showChangeLog(product);
    }

    private void showChangeLog(Product product) {
        List<StockChange> log = inventoryService.changeLogOf(admin(), product.id());
        io.print("Change log:");
        log.forEach(change -> io.print("  " + change.changedAt() + "  "
                + (change.isIncrease() ? "+" : "") + change.delta() + "  " + change.reason()));
    }

    private void createProduct() {
        Optional<Category> category = chooseCategory();
        if (category.isEmpty()) {
            return;
        }
        io.printTitle("Add a product");
        Product product = productAdminService.createProduct(admin(),
                io.readLine("Name"),
                io.readLine("Description"),
                Money.of(io.readLine("Price")),
                new StockCount(io.readInt("Starting stock", 0, Integer.MAX_VALUE)),
                category.get().id(),
                io.readLine("Brand"));
        io.printSuccess(product.name() + " is now on sale.");
    }

    private void editProduct() {
        Optional<Product> chosen = chooseProduct("Which product to edit", allProducts());
        if (chosen.isEmpty()) {
            return;
        }
        Optional<Category> category = chooseCategory();
        if (category.isEmpty()) {
            return;
        }
        Product product = chosen.get();
        io.printTitle("Edit " + product.name());
        productAdminService.updateProduct(admin(), product.id(),
                io.readLine("Name [" + product.name() + "]"),
                io.readLine("Description"),
                Money.of(io.readLine("Price [" + product.price() + "]")),
                category.get().id(),
                io.readLine("Brand [" + product.brand() + "]"));
        io.printSuccess("Product updated.");
    }

    private void deactivateProduct() {
        Optional<Product> chosen = chooseProduct("Which product to switch off",
                allProducts());
        if (chosen.isEmpty()) {
            return;
        }
        if (!io.confirm("Switch \"" + chosen.get().name() + "\" off?")) {
            return;
        }
        productAdminService.deactivateProduct(admin(), chosen.get().id());
        io.printSuccess("Customers can no longer buy it. Past orders are untouched.");
    }

    private void createCategory() {
        Category category = productAdminService.createCategory(admin(),
                io.readLine("Category name"));
        io.printSuccess("Category \"" + category.name() + "\" added.");
    }

    private void showOrdersByStatus() {
        List<OrderStatus> statuses = List.of(OrderStatus.values());
        io.chooseFrom("Which status", statuses,
                        statuses.stream().map(Enum::name).toList())
                .ifPresent(status -> showOrders(orderService.ordersByStatus(admin(), status)));
    }

    /**
     * EFFECTS: Lists orders without the gateway references, which user story 36 asks to be
     * kept out of sight unless they are needed.
     */
    private void showOrders(List<Order> orders) {
        io.printTitle(orders.size() + " order(s)");
        io.printNumberedList(orders.stream()
                .map(order -> order.placedAt().toLocalDate() + "  " + order.totalAmount()
                        + "  " + order.status() + "  " + order.shippingAddress().city())
                .toList(), "There are no orders yet.");
    }

    private Optional<Product> chooseProduct(String prompt, List<Product> products) {
        if (products.isEmpty()) {
            io.print("The shop has no products yet.");
            return Optional.empty();
        }
        return io.chooseFrom(prompt, products,
                products.stream()
                        .map(product -> product.name() + "  " + product.price())
                        .toList());
    }

    private Optional<Category> chooseCategory() {
        List<Category> categories = catalogService.listActiveCategories();
        if (categories.isEmpty()) {
            io.print("Add a category first.");
            return Optional.empty();
        }
        return io.chooseFrom("Which category", categories,
                categories.stream().map(Category::name).toList());
    }

    private List<Product> allProducts() {
        return inventoryService.viewStock(admin());
    }

    private User admin() {
        return session.requireCurrentUser();
    }
}
