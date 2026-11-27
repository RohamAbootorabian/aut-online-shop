package ir.aut.shop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ir.aut.shop.model.Address;
import ir.aut.shop.model.Cart;
import ir.aut.shop.model.Category;
import ir.aut.shop.model.CategoryId;
import ir.aut.shop.model.Email;
import ir.aut.shop.model.Money;
import ir.aut.shop.model.Order;
import ir.aut.shop.model.OrderId;
import ir.aut.shop.model.OrderStatus;
import ir.aut.shop.model.Payment;
import ir.aut.shop.model.PaymentId;
import ir.aut.shop.model.PhoneNumber;
import ir.aut.shop.model.PostalCode;
import ir.aut.shop.model.Product;
import ir.aut.shop.model.ProductId;
import ir.aut.shop.model.Quantity;
import ir.aut.shop.model.Rating;
import ir.aut.shop.model.Review;
import ir.aut.shop.model.ReviewId;
import ir.aut.shop.model.SearchPhrase;
import ir.aut.shop.model.SortOption;
import ir.aut.shop.model.StockChange;
import ir.aut.shop.model.StockCount;
import ir.aut.shop.model.User;
import ir.aut.shop.model.UserId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Walks the whole shop the way a customer and an admin would, through the services.
 *
 * <p>These tests stand in for the two payment domain stories from part one: one walk ends
 * with an approving gateway, the other with a refusing one followed by a retry.
 */
class ShopScenarioTest {

    private Map<ProductId, Product> products;
    private SimulatedPaymentGateway gateway;

    private UserService userService;
    private AddressService addressService;
    private CatalogService catalogService;
    private CartService cartService;
    private OrderService orderService;
    private PaymentService paymentService;
    private ReviewService reviewService;
    private InventoryService inventoryService;
    private ProductAdminService productAdminService;

    private User admin;
    private User customer;
    private Address shippingAddress;
    private Product book;

    @BeforeEach
    void setUp() {
        Map<UserId, User> users = new HashMap<>();
        Map<CategoryId, Category> categories = new HashMap<>();
        Map<UserId, Cart> carts = new HashMap<>();
        Map<OrderId, Order> orders = new HashMap<>();
        Map<PaymentId, Payment> payments = new HashMap<>();
        Map<ReviewId, Review> reviews = new HashMap<>();
        List<StockChange> stockChangeLog = new ArrayList<>();
        products = new HashMap<>();
        gateway = new SimulatedPaymentGateway();

        userService = new UserService(users);
        addressService = new AddressService(users);
        cartService = new CartService(carts, products);
        orderService = new OrderService(orders, users, products, cartService);
        reviewService = new ReviewService(reviews, orderService);
        catalogService = new CatalogService(products, categories, reviewService);
        paymentService = new PaymentService(payments, orderService, gateway);
        inventoryService = new InventoryService(products, stockChangeLog);
        productAdminService = new ProductAdminService(products, categories);

        admin = userService.registerAdmin(new Email("admin@shop.ir"),
                new PhoneNumber("09120000000"), "Shop Manager", "admin123");
        customer = userService.signUp(new Email("ali@test.com"),
                new PhoneNumber("09121112233"), "Ali Ahmadi", "secret123");
        shippingAddress = addressService.addAddress(customer.id(), "Ali Ahmadi",
                new PhoneNumber("09121112233"), "Iran", "Tehran", "Valiasr St, No 20",
                new PostalCode("1234567890"), "home");

        Category books = productAdminService.createCategory(admin, "Books");
        book = productAdminService.createProduct(admin, "Clean Code",
                "Robert C. Martin on writing readable code.", Money.of("950000"),
                new StockCount(10), books.id(), "Prentice Hall");
    }

    @Test
    @DisplayName("Story 1: the same email cannot be registered twice")
    void signUpRefusesADuplicateEmail() {
        assertThrows(IllegalArgumentException.class, () -> userService.signUp(
                new Email("ali@test.com"), new PhoneNumber("09125556677"), "Someone Else",
                "another123"));
    }

    @Test
    @DisplayName("Story 2: wrong credentials are refused")
    void logInNeedsTheRightPassword() {
        assertEquals(customer.id(), userService.logIn("ali@test.com", "secret123").id());
        assertThrows(IllegalArgumentException.class,
                () -> userService.logIn("ali@test.com", "wrong"));
    }

    @Test
    @DisplayName("Story 7: the first address added becomes the default one")
    void firstAddressBecomesDefault() {
        assertTrue(shippingAddress.isDefault());
        assertEquals(1, addressService.listAddresses(customer.id()).size());
    }

    @Test
    @DisplayName("Stories 20 to 23: a full walk from cart to a confirmed payment")
    void customerOrdersAndPaysSuccessfully() {
        gateway.approveEverything();
        cartService.addToCart(customer.id(), book.id(), new Quantity(2));

        Order order = orderService.startCheckout(customer.id(), shippingAddress.id());

        assertEquals(OrderStatus.AWAITING_PAYMENT, order.status());
        assertEquals(Money.of("1900000"), order.itemsSubtotal());
        assertEquals(order.itemsSubtotal().plus(orderService.shippingFee()),
                order.totalAmount());
        assertTrue(cartService.cartOf(customer.id()).isEmpty(), "the cart was not emptied");
        assertEquals(8, book.stock().value(), "stock was not reduced");

        Payment attempt = paymentService.pay(order.id(), customer.id());

        assertTrue(attempt.isSuccessful());
        assertTrue(order.isPaid());
    }

    @Test
    @DisplayName("Story 23: a paid order cannot be paid a second time")
    void aPaidOrderCannotBePaidAgain() {
        gateway.approveEverything();
        cartService.addToCart(customer.id(), book.id(), new Quantity(1));
        Order order = orderService.startCheckout(customer.id(), shippingAddress.id());
        paymentService.pay(order.id(), customer.id());

        assertThrows(IllegalStateException.class,
                () -> paymentService.pay(order.id(), customer.id()));
    }

    @Test
    @DisplayName("Story 24: a refused payment leaves the order payable, and a retry works")
    void aRefusedPaymentCanBeRetried() {
        gateway.refuseEverything("Insufficient funds");
        cartService.addToCart(customer.id(), book.id(), new Quantity(1));
        Order order = orderService.startCheckout(customer.id(), shippingAddress.id());

        Payment refused = paymentService.pay(order.id(), customer.id());

        assertFalse(refused.isSuccessful());
        assertEquals(OrderStatus.AWAITING_PAYMENT, order.status(),
                "a refused payment must not mark the order paid");
        assertTrue(order.isPayable());

        gateway.approveEverything();
        Payment retry = paymentService.pay(order.id(), customer.id());

        assertTrue(retry.isSuccessful());
        assertTrue(order.isPaid());
        assertEquals(2, paymentService.paymentsOf(order.id()).size());
    }

    @Test
    @DisplayName("Story 27: one customer cannot read another customer's order")
    void ordersAreVisibleOnlyToTheirOwner() {
        gateway.approveEverything();
        cartService.addToCart(customer.id(), book.id(), new Quantity(1));
        Order order = orderService.startCheckout(customer.id(), shippingAddress.id());

        User stranger = userService.signUp(new Email("sara@test.com"),
                new PhoneNumber("09127778899"), "Sara Karimi", "secret456");

        assertTrue(orderService.findOrderOfCustomer(order.id(), customer.id()).isPresent());
        assertTrue(orderService.findOrderOfCustomer(order.id(), stranger.id()).isEmpty());
    }

    @Test
    @DisplayName("Story 15: the cart cannot hold more units than the shelf has")
    void cartRefusesMoreThanTheStock() {
        assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart(customer.id(), book.id(), new Quantity(11)));
    }

    @Test
    @DisplayName("Stories 30 and 31: only a buyer may review, and only once")
    void onlyABuyerMayReviewAndOnlyOnce() {
        assertThrows(IllegalArgumentException.class, () -> reviewService.addReview(
                customer.id(), book.id(), "Looks nice.", new Rating(5)));

        gateway.approveEverything();
        cartService.addToCart(customer.id(), book.id(), new Quantity(1));
        Order order = orderService.startCheckout(customer.id(), shippingAddress.id());
        paymentService.pay(order.id(), customer.id());

        reviewService.addReview(customer.id(), book.id(), "Changed how I write code.",
                new Rating(5));

        assertEquals(1, reviewService.reviewCount(book.id()));
        assertEquals(5.0, reviewService.averageRating(book.id()));
        assertThrows(IllegalArgumentException.class, () -> reviewService.addReview(
                customer.id(), book.id(), "Second thoughts.", new Rating(3)));
    }

    @Test
    @DisplayName("Stories 28, 29, 33 to 36: a customer may not do an admin's job")
    void managementIsForAdminsOnly() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.viewStock(customer));
        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.changeStock(customer, book.id(), 5, "restock"));
        assertThrows(IllegalArgumentException.class,
                () -> productAdminService.deactivateProduct(customer, book.id()));
        assertThrows(IllegalArgumentException.class, () -> orderService.allOrders(customer));
        assertThrows(IllegalArgumentException.class,
                () -> orderService.ordersByStatus(customer, OrderStatus.PAID));
    }

    @Test
    @DisplayName("Story 29: every stock change is logged and never goes below zero")
    void stockChangesAreLoggedAndBounded() {
        inventoryService.changeStock(admin, book.id(), 5, "New delivery");

        assertEquals(15, book.stock().value());
        assertEquals(1, inventoryService.changeLogOf(admin, book.id()).size());
        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.changeStock(admin, book.id(), -100, "Oops"));
    }

    @Test
    @DisplayName("Story 35: switching a product off hides it but keeps past orders whole")
    void deactivatingHidesTheProductButKeepsOrders() {
        gateway.approveEverything();
        cartService.addToCart(customer.id(), book.id(), new Quantity(1));
        Order order = orderService.startCheckout(customer.id(), shippingAddress.id());
        paymentService.pay(order.id(), customer.id());

        productAdminService.deactivateProduct(admin, book.id());

        assertTrue(catalogService.listActiveProducts().isEmpty(),
                "a switched-off product is still on sale");
        assertEquals(1, orderService.ordersOf(customer.id()).size());
        assertEquals("Clean Code",
                orderService.ordersOf(customer.id()).get(0).lines().get(0).productName());
    }

    @Test
    @DisplayName("Stories 10 and 12: searching finds the product, sorting arranges it")
    void searchingAndSortingWork() {
        productAdminService.createProduct(admin, "Refactoring", "Martin Fowler on change.",
                Money.of("1100000"), new StockCount(4), book.categoryId(), "Addison-Wesley");

        assertEquals(1, catalogService.search(new SearchPhrase("clean")).size());
        assertTrue(catalogService.search(new SearchPhrase("nothing here")).isEmpty());

        List<Product> cheapestFirst =
                catalogService.sort(catalogService.listActiveProducts(), SortOption.CHEAPEST);

        assertEquals("Clean Code", cheapestFirst.get(0).name());
        assertEquals("Refactoring", cheapestFirst.get(1).name());
    }

    @Test
    @DisplayName("Story 11: the lowest price may not be above the highest")
    void filterRefusesAnImpossiblePriceRange() {
        assertThrows(IllegalArgumentException.class, () -> new ProductFilterRequest(
                null, Money.of("100"), Money.of("10"), null, null, false));
    }

    @Test
    @DisplayName("Story 20: an order cannot be placed from an empty cart")
    void checkoutRefusesAnEmptyCart() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.startCheckout(customer.id(), shippingAddress.id()));
    }
}
