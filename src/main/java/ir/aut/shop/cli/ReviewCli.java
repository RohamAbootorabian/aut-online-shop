package ir.aut.shop.cli;

import ir.aut.shop.model.Order;
import ir.aut.shop.model.OrderLine;
import ir.aut.shop.model.Rating;
import ir.aut.shop.model.User;
import ir.aut.shop.service.OrderService;
import ir.aut.shop.service.ReviewService;
import java.util.List;
import java.util.Optional;

/**
 * Writing an opinion and a score about something already bought
 * (user stories 30 and 31).
 *
 * <p>The products offered here come from the customer's own paid orders, which is how the
 * screen matches the rule that only a buyer may review.
 */
public class ReviewCli {

    private static final int LOWEST_RATING = 1;
    private static final int HIGHEST_RATING = 5;

    private final ConsoleIo io;
    private final Session session;
    private final OrderService orderService;
    private final ReviewService reviewService;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds the screen over the given console, session and services.
     *
     * @param io where questions are asked and answers shown
     * @param session who is signed in
     * @param orderService supplies what the customer has bought
     * @param reviewService publishes the review
     */
    public ReviewCli(ConsoleIo io, Session session, OrderService orderService,
            ReviewService reviewService) {
        this.io = io;
        this.session = session;
        this.orderService = orderService;
        this.reviewService = reviewService;
    }

    /**
     * MODIFIES: the review store.
     * EFFECTS: Lets the customer pick something they have bought and review it, or says
     * there is nothing to review yet.
     */
    public void run() {
        User customer = session.requireCurrentUser();
        List<OrderLine> bought = purchasedLines(customer);

        io.printTitle("Write a review");
        if (bought.isEmpty()) {
            io.print("You can review a product once you have bought and paid for it.");
            return;
        }

        Optional<OrderLine> chosen = io.chooseFrom("Which product", bought,
                bought.stream().map(OrderLine::productName).toList());
        if (chosen.isEmpty()) {
            return;
        }

        try {
            String text = io.readLine("Your review");
            Rating rating = new Rating(io.readInt("Your rating", LOWEST_RATING,
                    HIGHEST_RATING));
            reviewService.addReview(customer.id(), chosen.get().productId(), text, rating);
            io.printSuccess("Thank you, your review has been published.");
        } catch (IllegalArgumentException problem) {
            io.printError(problem.getMessage());
        }
    }

    /** EFFECTS: Returns one line per distinct product the customer has paid for. */
    private List<OrderLine> purchasedLines(User customer) {
        return orderService.ordersOf(customer.id()).stream()
                .filter(Order::isPaid)
                .flatMap(order -> order.lines().stream())
                .collect(java.util.stream.Collectors.toMap(OrderLine::productId,
                        line -> line, (first, second) -> first))
                .values().stream()
                .toList();
    }
}
