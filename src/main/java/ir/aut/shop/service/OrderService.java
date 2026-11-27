package ir.aut.shop.service;

import ir.aut.shop.model.Address;
import ir.aut.shop.model.AddressId;
import ir.aut.shop.model.Cart;
import ir.aut.shop.model.CartLine;
import ir.aut.shop.model.Money;
import ir.aut.shop.model.Order;
import ir.aut.shop.model.OrderId;
import ir.aut.shop.model.OrderLine;
import ir.aut.shop.model.OrderStatus;
import ir.aut.shop.model.Product;
import ir.aut.shop.model.ProductId;
import ir.aut.shop.model.User;
import ir.aut.shop.model.UserId;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Turning a cart into an order and looking orders up afterwards
 * (user stories 20 to 22, 26, 27 and 36).
 */
public class OrderService {

    /** What delivery costs; a single flat fee is enough for this phase. */
    private static final Money SHIPPING_FEE = Money.of("50000");

    private final Map<OrderId, Order> orders;
    private final Map<UserId, User> users;
    private final Map<ProductId, Product> products;
    private final CartService cartService;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds the service over the given stores.
     *
     * @param orders where orders are kept, keyed by identity
     * @param users where accounts are kept, keyed by identity
     * @param products where products are kept, keyed by identity
     * @param cartService the carts orders are placed from
     */
    public OrderService(Map<OrderId, Order> orders, Map<UserId, User> users,
            Map<ProductId, Product> products, CartService cartService) {
        if (orders == null || users == null || products == null || cartService == null) {
            throw new IllegalArgumentException("OrderService is missing a dependency");
        }
        this.orders = orders;
        this.users = users;
        this.products = products;
        this.cartService = cartService;
    }

    /**
     * Returns what delivery costs.
     *
     * @return the flat shipping fee added to every order
     */
    public Money shippingFee() {
        return SHIPPING_FEE;
    }

    /**
     * REQUIRES: customerId and shippingAddressId are non-null.
     * MODIFIES: the order store, the customer's cart and the stock of every ordered
     * product.
     * EFFECTS: Places an order for everything in the cart and returns it, awaiting
     * payment. The cart is emptied and the stock is reduced. Throws
     * IllegalArgumentException when the cart is empty, when the address does not belong to
     * the customer, or when any product is no longer buyable in the wanted quantity
     * (user stories 20 and 21).
     *
     * <p>The amount is worked out from the captured line prices; a figure supplied by the
     * customer is never used (user story 22).
     *
     * @param customerId who is ordering
     * @param shippingAddressId where the parcel should go
     * @return the newly placed order
     */
    public Order startCheckout(UserId customerId, AddressId shippingAddressId) {
        User customer = requireUser(customerId);
        Cart cart = cartService.cartOf(customerId);
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order from an empty cart");
        }
        Address shippingAddress = customer.findAddress(shippingAddressId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Address " + shippingAddressId + " does not belong to this customer"));

        List<CartLine> cartLines = cart.lines();
        requireEveryLineStillAvailable(cartLines);

        List<OrderLine> orderLines = cartLines.stream()
                .map(OrderLine::from)
                .toList();
        Order order = new Order(OrderId.newId(), customerId, orderLines, shippingAddress,
                SHIPPING_FEE, LocalDateTime.now());

        takeOrderedUnitsOffTheShelf(orderLines);
        cartService.clearCart(customerId);
        orders.put(order.id(), order);
        return order;
    }

    /**
     * REQUIRES: orderId is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the order with that identity, or empty when there is none.
     *
     * @param orderId the order to look up
     * @return the order, or empty
     */
    public Optional<Order> findOrder(OrderId orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    /**
     * REQUIRES: orderId and customerId are non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the order only when it belongs to that customer, or empty
     * otherwise. This is what keeps one customer from reading another's order
     * (user story 27).
     *
     * @param orderId the order to look up
     * @param customerId who is asking
     * @return the order when it is theirs, or empty
     */
    public Optional<Order> findOrderOfCustomer(OrderId orderId, UserId customerId) {
        return findOrder(orderId)
                .filter(order -> order.customerId().equals(customerId));
    }

    /**
     * REQUIRES: customerId is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns that customer's orders, newest first (user story 26).
     *
     * @param customerId whose orders to list
     * @return the customer's orders
     */
    public List<Order> ordersOf(UserId customerId) {
        return orders.values().stream()
                .filter(order -> order.customerId().equals(customerId))
                .sorted(Comparator.comparing(Order::placedAt).reversed())
                .toList();
    }

    /**
     * REQUIRES: actor is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns every order in the shop, newest first, or throws
     * IllegalArgumentException when the actor is not an admin (user story 36).
     *
     * @param actor who is asking
     * @return every order
     */
    public List<Order> allOrders(User actor) {
        requireAdmin(actor);
        return orders.values().stream()
                .sorted(Comparator.comparing(Order::placedAt).reversed())
                .toList();
    }

    /**
     * REQUIRES: actor and status are non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns every order in that state, newest first, or throws
     * IllegalArgumentException when the actor is not an admin (user story 36).
     *
     * @param actor who is asking
     * @param status the state to filter by
     * @return the matching orders
     */
    public List<Order> ordersByStatus(User actor, OrderStatus status) {
        requireAdmin(actor);
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null");
        }
        return allOrders(actor).stream()
                .filter(order -> order.status() == status)
                .toList();
    }

    /**
     * REQUIRES: customerId and productId are non-null.
     * MODIFIES: nothing.
     * EFFECTS: Reports whether the customer has a paid order containing the product. User
     * stories 30 and 31 let only a buyer review a product.
     *
     * @param customerId who wants to review
     * @param productId the product they want to review
     * @return true when they have bought it
     */
    public boolean hasPurchased(UserId customerId, ProductId productId) {
        return orders.values().stream()
                .filter(order -> order.customerId().equals(customerId))
                .filter(Order::isPaid)
                .flatMap(order -> order.lines().stream())
                .anyMatch(line -> line.productId().equals(productId));
    }

    private void requireEveryLineStillAvailable(List<CartLine> cartLines) {
        for (CartLine line : cartLines) {
            Product product = products.get(line.productId());
            if (product == null || !product.isActive()) {
                throw new IllegalArgumentException(
                        line.productName() + " is no longer on sale");
            }
            if (!product.hasEnoughStock(line.quantity())) {
                throw new IllegalArgumentException("Only " + product.stock()
                        + " units of " + product.name() + " are in stock");
            }
        }
    }

    private void takeOrderedUnitsOffTheShelf(List<OrderLine> orderLines) {
        for (OrderLine line : orderLines) {
            Product product = products.get(line.productId());
            product.reduceStock(line.quantity());
            product.recordSale(line.quantity());
        }
    }

    private User requireUser(UserId userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("No account with id " + userId);
        }
        return user;
    }

    private static void requireAdmin(User actor) {
        if (actor == null || !actor.isAdmin()) {
            throw new IllegalArgumentException("Only an admin may do this");
        }
    }
}
