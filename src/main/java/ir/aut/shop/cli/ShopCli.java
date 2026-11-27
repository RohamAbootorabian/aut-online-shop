package ir.aut.shop.cli;

/**
 * The root menu of the shop.
 *
 * <p>What the menu offers depends on who is signed in, so a visitor is never shown a cart
 * and a customer is never shown the management screens. The services check the same rules
 * again, so hiding a menu entry is a convenience, not the protection itself.
 */
public class ShopCli {

    private final ConsoleIo io;
    private final Session session;
    private final AuthCli authCli;
    private final ProfileCli profileCli;
    private final CatalogCli catalogCli;
    private final CartCli cartCli;
    private final CheckoutCli checkoutCli;
    private final OrderCli orderCli;
    private final ReviewCli reviewCli;
    private final AdminCli adminCli;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds the root menu over the given screens.
     *
     * @param io where questions are asked and answers shown
     * @param session who is signed in
     * @param authCli signing up, in and out
     * @param profileCli profile and address book
     * @param catalogCli browsing the catalogue
     * @param cartCli the shopping cart
     * @param checkoutCli ordering and paying
     * @param orderCli past orders
     * @param reviewCli writing reviews
     * @param adminCli the management screens
     */
    public ShopCli(ConsoleIo io, Session session, AuthCli authCli, ProfileCli profileCli,
            CatalogCli catalogCli, CartCli cartCli, CheckoutCli checkoutCli,
            OrderCli orderCli, ReviewCli reviewCli, AdminCli adminCli) {
        this.io = io;
        this.session = session;
        this.authCli = authCli;
        this.profileCli = profileCli;
        this.catalogCli = catalogCli;
        this.cartCli = cartCli;
        this.checkoutCli = checkoutCli;
        this.orderCli = orderCli;
        this.reviewCli = reviewCli;
        this.adminCli = adminCli;
    }

    /**
     * MODIFIES: everything the chosen screens modify.
     * EFFECTS: Shows the menu over and over until the visitor chooses to leave.
     */
    public void run() {
        io.print("Welcome to the shop.");
        while (true) {
            boolean keepGoing = session.isLoggedIn() ? runSignedInMenu() : runVisitorMenu();
            if (!keepGoing) {
                io.print("Goodbye.");
                return;
            }
        }
    }

    /** EFFECTS: Shows the menu for somebody who has not signed in. */
    private boolean runVisitorMenu() {
        io.printTitle("Main menu");
        io.print("  1) Browse the catalogue");
        io.print("  2) Sign up");
        io.print("  3) Log in");
        io.print("  4) Forgotten password");
        io.print("  0) Exit");

        return switch (io.readInt("Choose", 0, 4)) {
            case 1 -> keepGoingAfter(catalogCli::run);
            case 2 -> keepGoingAfter(authCli::signUp);
            case 3 -> keepGoingAfter(authCli::logIn);
            case 4 -> keepGoingAfter(authCli::recoverPassword);
            default -> false;
        };
    }

    /** EFFECTS: Shows the menu for a signed-in customer, with the extra admin entry. */
    private boolean runSignedInMenu() {
        boolean isAdmin = session.isAdminLoggedIn();
        io.printTitle("Main menu - " + session.requireCurrentUser().fullName());
        io.print("  1) Browse the catalogue");
        io.print("  2) My cart");
        io.print("  3) Checkout");
        io.print("  4) My orders");
        io.print("  5) Write a review");
        io.print("  6) My profile");
        io.print("  7) Log out");
        if (isAdmin) {
            io.print("  8) Manage the shop");
        }
        io.print("  0) Exit");

        int highest = isAdmin ? 8 : 7;
        return switch (io.readInt("Choose", 0, highest)) {
            case 1 -> keepGoingAfter(catalogCli::run);
            case 2 -> keepGoingAfter(cartCli::run);
            case 3 -> keepGoingAfter(checkoutCli::run);
            case 4 -> keepGoingAfter(orderCli::run);
            case 5 -> keepGoingAfter(reviewCli::run);
            case 6 -> keepGoingAfter(profileCli::run);
            case 7 -> keepGoingAfter(authCli::logOut);
            case 8 -> keepGoingAfter(adminCli::run);
            default -> false;
        };
    }

    /**
     * EFFECTS: Runs one screen and reports that the shop should keep running. A problem
     * inside a screen is shown and the menu comes back, rather than the program stopping.
     */
    private boolean keepGoingAfter(Runnable screen) {
        try {
            screen.run();
        } catch (IllegalArgumentException | IllegalStateException problem) {
            io.printError(problem.getMessage());
        }
        return true;
    }
}
