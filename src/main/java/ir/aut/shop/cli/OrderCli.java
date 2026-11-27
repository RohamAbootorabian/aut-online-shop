package ir.aut.shop.cli;

import ir.aut.shop.model.Order;
import ir.aut.shop.model.OrderLine;
import ir.aut.shop.model.Payment;
import ir.aut.shop.model.User;
import ir.aut.shop.service.OrderService;
import ir.aut.shop.service.PaymentService;
import java.util.List;
import java.util.Optional;

/**
 * Looking back at past orders and retrying an unpaid one
 * (user stories 24, 26 and 27).
 */
public class OrderCli {

    private final ConsoleIo io;
    private final Session session;
    private final OrderService orderService;
    private final PaymentService paymentService;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds the screen over the given console, session and services.
     *
     * @param io where questions are asked and answers shown
     * @param session who is signed in
     * @param orderService supplies the customer's orders
     * @param paymentService used to retry an unpaid order
     */
    public OrderCli(ConsoleIo io, Session session, OrderService orderService,
            PaymentService paymentService) {
        this.io = io;
        this.session = session;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    /**
     * MODIFIES: the payment store, when the customer retries an unpaid order.
     * EFFECTS: Lists the customer's orders and lets them open one.
     */
    public void run() {
        User customer = session.requireCurrentUser();
        List<Order> orders = orderService.ordersOf(customer.id());

        io.printTitle("My orders");
        io.printNumberedList(labelsOf(orders), "You have not ordered anything yet.");
        if (orders.isEmpty() || !io.confirm("Open one of them?")) {
            return;
        }

        Optional<Order> chosen = io.chooseFrom("Which order", orders, labelsOf(orders));
        chosen.ifPresent(this::showOrderDetails);
    }

    /** EFFECTS: Shows one order in full (user story 27). */
    private void showOrderDetails(Order order) {
        io.printTitle("Order " + order.id());
        io.print("Placed  : " + order.placedAt());
        io.print("Status  : " + order.status());
        io.print("Ship to : " + order.shippingAddress());
        io.print("Items   :");
        for (OrderLine line : order.lines()) {
            io.print("  " + line.quantity() + " x " + line.productName()
                    + " @ " + line.unitPrice() + " = " + line.lineTotal());
        }
        io.print("Subtotal: " + order.itemsSubtotal());
        io.print("Shipping: " + order.shippingFee());
        io.print("Total   : " + order.totalAmount());
        showPaymentAttempts(order);

        if (order.isPayable() && io.confirm("Pay for this order now?")) {
            retryPayment(order);
        }
    }

    private void showPaymentAttempts(Order order) {
        List<Payment> attempts = paymentService.paymentsOf(order.id());
        if (attempts.isEmpty()) {
            return;
        }
        io.print("Payments:");
        attempts.forEach(attempt -> io.print("  " + attempt.attemptedAt() + "  "
                + attempt.status()));
    }

    /**
     * MODIFIES: the payment store and possibly the order.
     * EFFECTS: Tries the payment again, which user story 24 allows while the order is
     * still payable.
     */
    private void retryPayment(Order order) {
        try {
            Payment attempt = paymentService.pay(order.id(),
                    session.requireCurrentUser().id());
            if (attempt.isSuccessful()) {
                io.printSuccess("Payment confirmed. Reference "
                        + attempt.gatewayReference() + ".");
            } else {
                io.printError("The payment was refused again.");
            }
        } catch (IllegalArgumentException | IllegalStateException problem) {
            io.printError(problem.getMessage());
        }
    }

    private List<String> labelsOf(List<Order> orders) {
        return orders.stream()
                .map(order -> order.placedAt().toLocalDate() + "  " + order.totalAmount()
                        + "  " + order.status() + "  (" + order.lines().size() + " item(s))")
                .toList();
    }
}
