package ir.aut.shop.cli;

import ir.aut.shop.model.Category;
import ir.aut.shop.model.Money;
import ir.aut.shop.model.Product;
import ir.aut.shop.model.Quantity;
import ir.aut.shop.model.Rating;
import ir.aut.shop.model.Review;
import ir.aut.shop.model.SearchPhrase;
import ir.aut.shop.model.SortOption;
import ir.aut.shop.service.CartService;
import ir.aut.shop.service.CatalogService;
import ir.aut.shop.service.ProductFilterRequest;
import ir.aut.shop.service.ReviewService;
import java.util.List;
import java.util.Optional;

/**
 * Browsing, searching, filtering and sorting the catalogue, and reading one product
 * (user stories 8 to 14 and 32).
 */
public class CatalogCli {

    private static final int ALL_PRODUCTS = 1;
    private static final int BY_CATEGORY = 2;
    private static final int SEARCH = 3;
    private static final int FILTER = 4;
    private static final int BACK = 0;

    private final ConsoleIo io;
    private final Session session;
    private final CatalogService catalogService;
    private final CartService cartService;
    private final ReviewService reviewService;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds the screen over the given console, session and services.
     *
     * @param io where questions are asked and answers shown
     * @param session who is signed in
     * @param catalogService the catalogue behind the screen
     * @param cartService used when a customer adds something to their cart
     * @param reviewService supplies ratings and opinions
     */
    public CatalogCli(ConsoleIo io, Session session, CatalogService catalogService,
            CartService cartService, ReviewService reviewService) {
        this.io = io;
        this.session = session;
        this.catalogService = catalogService;
        this.cartService = cartService;
        this.reviewService = reviewService;
    }

    /**
     * EFFECTS: Runs the catalogue menu until the visitor goes back.
     */
    public void run() {
        while (true) {
            io.printTitle("Catalogue");
            io.print("  1) All products");
            io.print("  2) Browse by category");
            io.print("  3) Search");
            io.print("  4) Filter");
            io.print("  0) Back");

            int choice = io.readInt("Choose", BACK, FILTER);
            try {
                switch (choice) {
                    case ALL_PRODUCTS -> showProducts(catalogService.listActiveProducts());
                    case BY_CATEGORY -> browseByCategory();
                    case SEARCH -> search();
                    case FILTER -> filter();
                    default -> {
                        return;
                    }
                }
            } catch (IllegalArgumentException problem) {
                io.printError(problem.getMessage());
            }
        }
    }

    private void browseByCategory() {
        List<Category> categories = catalogService.listActiveCategories();
        if (categories.isEmpty()) {
            io.print("The shop has no categories yet.");
            return;
        }
        Optional<Category> chosen = io.chooseFrom("Which category", categories,
                categories.stream().map(Category::name).toList());
        chosen.ifPresent(category -> {
            List<Product> found = catalogService.productsInCategory(category.id());
            if (found.isEmpty()) {
                io.print("There is nothing in " + category.name() + " yet.");
                return;
            }
            showProducts(found);
        });
    }

    private void search() {
        SearchPhrase phrase = new SearchPhrase(io.readLine("What are you looking for"));
        List<Product> found = catalogService.search(phrase);
        if (found.isEmpty()) {
            io.print("Nothing matched \"" + phrase + "\".");
            return;
        }
        showProducts(found);
    }

    private void filter() {
        io.printTitle("Filter");
        Optional<String> brand = io.readOptionalLine("Brand");
        Optional<Money> lowest = io.readOptionalLine("Lowest price").map(Money::of);
        Optional<Money> highest = io.readOptionalLine("Highest price").map(Money::of);
        Optional<Rating> minRating = io.readOptionalLine("Minimum rating 1-5")
                .map(text -> new Rating(Integer.parseInt(text)));
        boolean inStockOnly = io.confirm("Only products in stock?");

        ProductFilterRequest request = new ProductFilterRequest(null, lowest.orElse(null),
                highest.orElse(null), brand.orElse(null), minRating.orElse(null), inStockOnly);
        List<Product> found = catalogService.filter(request);
        if (found.isEmpty()) {
            io.print("No product passed that filter.");
            return;
        }
        showProducts(found);
    }

    /**
     * EFFECTS: Shows a product list, offers to re-order it, and lets the visitor open one
     * product.
     */
    private void showProducts(List<Product> found) {
        List<Product> shown = found;
        if (shown.size() > 1 && io.confirm("Sort the results?")) {
            shown = catalogService.sort(shown, askForSortOption());
        }
        io.printTitle(shown.size() + " product(s)");
        io.printNumberedList(labelsOf(shown), "Nothing to show.");

        if (io.confirm("Open one of them?")) {
            io.chooseFrom("Which product", shown, labelsOf(shown))
                    .ifPresent(this::showProductDetails);
        }
    }

    private SortOption askForSortOption() {
        List<SortOption> options = List.of(SortOption.values());
        return io.chooseFrom("Sort by", options,
                        options.stream().map(this::labelOf).toList())
                .orElse(SortOption.NEWEST);
    }

    private String labelOf(SortOption option) {
        return switch (option) {
            case CHEAPEST -> "Cheapest first";
            case MOST_EXPENSIVE -> "Most expensive first";
            case NEWEST -> "Newest first";
            case MOST_POPULAR -> "Most popular first";
            case HIGHEST_RATED -> "Highest rated first";
        };
    }

    /**
     * EFFECTS: Shows one product in full, with its rating and opinions, and offers to put
     * it in the cart (user stories 9 and 32).
     */
    private void showProductDetails(Product product) {
        io.printTitle(product.name());
        io.print("Brand       : " + product.brand());
        io.print("Price       : " + product.price());
        io.print("In stock    : " + product.stock());
        io.print("Category    : " + categoryNameOf(product));
        io.print("Description : " + product.description());
        io.print("Pictures    : " + picturesOf(product));
        io.print("Rating      : " + ratingTextOf(product));
        showReviews(product);

        if (!product.isPurchasable()) {
            io.print("This product cannot be bought right now.");
            return;
        }
        if (!session.isLoggedIn()) {
            io.print("Sign in to put this in your cart.");
            return;
        }
        if (io.confirm("Add to cart?")) {
            addToCart(product);
        }
    }

    private void addToCart(Product product) {
        int wanted = io.readInt("How many", 1, product.stock().value());
        cartService.addToCart(session.requireCurrentUser().id(), product.id(),
                new Quantity(wanted));
        io.printSuccess(wanted + " x " + product.name() + " added to your cart.");
    }

    /**
     * EFFECTS: Shows the opinions left on a product. Only the reviewer's initial is shown,
     * because user story 32 forbids revealing their personal details.
     */
    private void showReviews(Product product) {
        List<Review> reviews = reviewService.reviewsOf(product.id());
        if (reviews.isEmpty()) {
            io.print("Reviews     : none yet");
            return;
        }
        io.print("Reviews     :");
        reviews.forEach(review -> io.print("  - " + review.rating() + "  " + review.text()));
    }

    private String ratingTextOf(Product product) {
        int count = reviewService.reviewCount(product.id());
        if (count == 0) {
            return "not rated yet";
        }
        double average = reviewService.averageRating(product.id());
        return String.format("%.1f/5 from %d review(s)", average, count);
    }

    private String categoryNameOf(Product product) {
        return catalogService.findCategory(product.categoryId())
                .map(Category::name)
                .orElse("unknown");
    }

    private String picturesOf(Product product) {
        List<String> urls = product.imageUrls();
        return urls.isEmpty() ? "none" : String.join(", ", urls);
    }

    private List<String> labelsOf(List<Product> shown) {
        return shown.stream()
                .map(product -> product.name() + " - " + product.price()
                        + " (" + product.brand() + ", " + product.stock() + " in stock)")
                .toList();
    }
}
