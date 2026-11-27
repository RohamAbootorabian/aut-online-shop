package ir.aut.shop.cli;

import ir.aut.shop.model.Address;
import ir.aut.shop.model.Cart;
import ir.aut.shop.model.CartLine;
import ir.aut.shop.model.Order;
import ir.aut.shop.model.Payment;
import ir.aut.shop.model.User;
import ir.aut.shop.service.AddressService;
import ir.aut.shop.service.CartService;
import ir.aut.shop.service.OrderService;
import ir.aut.shop.service.PaymentService;
import java.util.List;
import java.util.Optional;

/**
 * Turning a cart into a paid order (user stories 20 to 25).
 *
 * <p>The two outcomes of a payment are the two separate domain stories from part one:
 * an approval ends the walk here, a refusal leaves the order payable so the customer may
 * try again.
 */
public class CheckoutCli {

    private final ConsoleIo io;
    private final Session session;
    private final CartService cartService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds the screen over the given console, session and services.
     *
     * @param io where questions are asked and answers shown
     * @param session who is signed in
     * @param cartService supplies what is being ordered
     * @param addressService supplies where it should go
     * @param orderService places the order
     * @param paymentService pays for it
     */
    public CheckoutCli(ConsoleIo io, Session session, CartService cartService,
            AddressService addressService, OrderService orderService,
            PaymentService paymentService) {
        this.io = io;
        this.session = session;
        this.cartService = cartService;
        this.addressService = addressService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    /**
     * MODIFIES: the customer's cart, the order store, the payment store and product stock.
     * EFFECTS: Walks the customer from cart to payment. Any refusal is reported and the
     * walk stops without leaving the shop in a half-finished state.
     */
    public void run() {
        User customer = session.requireCurrentUser();
        Cart cart = cartService.cartOf(customer.id());
        if (cart.isEmpty()) {
            io.print("Your cart is empty, so there is nothing to order.");
            return;
        }

        try {
            Optional<Address> shippingAddress = chooseShippingAddress();
            if (shippingAddress.isEmpty()) {
                return;
            }
            showOrderSummary(cart, shippingAddress.get());
            if (!io.confirm("Place this order?")) {
                return;
            }

            Order order = orderService.startCheckout(customer.id(),
                    shippingAddress.get().id());
            io.printSuccess("Order " + order.id() + " placed, awaiting payment.");
            payFor(order);
        } catch (IllegalArgumentException | IllegalStateException problem) {
            io.printError(problem.getMessage());
        }
    }

    /** EFFECTS: Shows what will be ordered and what it costs (user story 22). */
    private void showOrderSummary(Cart cart, Address shippingAddress) {
        io.printTitle("Order summary");
        for (CartLine line : cart.lines()) {
            io.print("  " + line.quantity() + " x " + line.productName()
                    + " = " + line.lineTotal());
        }
        io.print("Items    : " + cart.itemsSubtotal());
        io.print("Shipping : " + orderService.shippingFee());
        io.print("Total    : " + cart.itemsSubtotal().plus(orderService.shippingFee()));
        io.print("Ship to  : " + shippingAddress);
    }

    /**
     * MODIFIES: the payment store and possibly the order.
     * EFFECTS: Asks the gateway for the order total and reports which of the two outcomes
     * happened (user stories 23 and 24).
     */
    private void payFor(Order order) {
        if (!io.confirm("Pay " + order.totalAmount() + " now?")) {
            io.print("You can pay for this order later from My orders.");
            return;
        }
        Payment attempt = paymentService.pay(order.id(), session.requireCurrentUser().id());
        if (attempt.isSuccessful()) {
            io.printSuccess("Payment confirmed. Reference " + attempt.gatewayReference() + ".");
            return;
        }
        io.printError("The payment was refused. The order is still waiting, "
                + "so you can try again from My orders.");
    }

    private Optional<Address> chooseShippingAddress() {
        List<Address> addresses = addressService.listAddresses(
                session.requireCurrentUser().id());
        if (addresses.isEmpty()) {
            io.print("Add an address in My profile before ordering.");
            return Optional.empty();
        }
        return io.chooseFrom("Ship to which address", addresses,
                addresses.stream()
                        .map(address -> address + (address.isDefault() ? "  [default]" : ""))
                        .toList());
    }
}
