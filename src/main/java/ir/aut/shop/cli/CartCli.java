package ir.aut.shop.cli;

import ir.aut.shop.model.Cart;
import ir.aut.shop.model.CartLine;
import ir.aut.shop.model.Quantity;
import ir.aut.shop.service.CartService;
import java.util.List;
import java.util.Optional;

/**
 * Looking at and changing the shopping cart (user stories 16 to 19).
 */
public class CartCli {

    private static final int CHANGE_QUANTITY = 1;
    private static final int REMOVE_LINE = 2;
    private static final int EMPTY_CART = 3;
    private static final int BACK = 0;

    private final ConsoleIo io;
    private final Session session;
    private final CartService cartService;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds the screen over the given console, session and service.
     *
     * @param io where questions are asked and answers shown
     * @param session who is signed in
     * @param cartService the carts behind the screen
     */
    public CartCli(ConsoleIo io, Session session, CartService cartService) {
        this.io = io;
        this.session = session;
        this.cartService = cartService;
    }

    /**
     * MODIFIES: the signed-in customer's cart.
     * EFFECTS: Runs the cart menu until the customer goes back.
     */
    public void run() {
        while (true) {
            showCart();
            io.print("  1) Change a quantity");
            io.print("  2) Remove a product");
            io.print("  3) Empty the cart");
            io.print("  0) Back");

            int choice = io.readInt("Choose", BACK, EMPTY_CART);
            try {
                switch (choice) {
                    case CHANGE_QUANTITY -> changeQuantity();
                    case REMOVE_LINE -> removeLine();
                    case EMPTY_CART -> emptyCart();
                    default -> {
                        return;
                    }
                }
            } catch (IllegalArgumentException problem) {
                io.printError(problem.getMessage());
            }
        }
    }

    /** EFFECTS: Shows the cart and the total the shop worked out (user story 16). */
    private void showCart() {
        Cart cart = currentCart();
        io.printTitle("My cart");
        io.printNumberedList(labelsOf(cart.lines()), "Your cart is empty.");
        io.print("Total: " + cart.itemsSubtotal());
    }

    private void changeQuantity() {
        Optional<CartLine> chosen = chooseLine("Which product");
        if (chosen.isEmpty()) {
            return;
        }
        int wanted = io.readInt("New quantity", 1, Integer.MAX_VALUE);
        cartService.changeQuantity(session.requireCurrentUser().id(),
                chosen.get().productId(), new Quantity(wanted));
        io.printSuccess("Quantity updated.");
    }

    private void removeLine() {
        Optional<CartLine> chosen = chooseLine("Which product to remove");
        if (chosen.isEmpty()) {
            return;
        }
        cartService.removeFromCart(session.requireCurrentUser().id(),
                chosen.get().productId());
        io.printSuccess(chosen.get().productName() + " removed from your cart.");
    }

    private void emptyCart() {
        if (currentCart().isEmpty()) {
            io.print("Your cart is already empty.");
            return;
        }
        if (!io.confirm("Remove everything from your cart?")) {
            return;
        }
        cartService.clearCart(session.requireCurrentUser().id());
        io.printSuccess("Your cart is now empty.");
    }

    private Optional<CartLine> chooseLine(String prompt) {
        List<CartLine> lines = currentCart().lines();
        if (lines.isEmpty()) {
            io.print("Your cart is empty.");
            return Optional.empty();
        }
        return io.chooseFrom(prompt, lines, labelsOf(lines));
    }

    private Cart currentCart() {
        return cartService.cartOf(session.requireCurrentUser().id());
    }

    private List<String> labelsOf(List<CartLine> lines) {
        return lines.stream()
                .map(line -> line.quantity() + " x " + line.productName()
                        + " @ " + line.unitPrice() + " = " + line.lineTotal())
                .toList();
    }
}
