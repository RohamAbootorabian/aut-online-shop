package ir.aut.shop;

import ir.aut.shop.cli.AdminCli;
import ir.aut.shop.cli.AuthCli;
import ir.aut.shop.cli.CartCli;
import ir.aut.shop.cli.CatalogCli;
import ir.aut.shop.cli.CheckoutCli;
import ir.aut.shop.cli.ConsoleIo;
import ir.aut.shop.cli.OrderCli;
import ir.aut.shop.cli.ProfileCli;
import ir.aut.shop.cli.ReviewCli;
import ir.aut.shop.cli.Session;
import ir.aut.shop.cli.ShopCli;
import ir.aut.shop.model.Category;
import ir.aut.shop.model.Email;
import ir.aut.shop.model.Money;
import ir.aut.shop.model.PhoneNumber;
import ir.aut.shop.model.StockCount;
import ir.aut.shop.model.User;
import ir.aut.shop.repository.CartRepository;
import ir.aut.shop.repository.CategoryRepository;
import ir.aut.shop.repository.OrderRepository;
import ir.aut.shop.repository.PaymentRepository;
import ir.aut.shop.repository.ProductRepository;
import ir.aut.shop.repository.ReviewRepository;
import ir.aut.shop.repository.StockChangeLog;
import ir.aut.shop.repository.UserRepository;
import ir.aut.shop.repository.memory.InMemoryCartRepository;
import ir.aut.shop.repository.memory.InMemoryCategoryRepository;
import ir.aut.shop.repository.memory.InMemoryOrderRepository;
import ir.aut.shop.repository.memory.InMemoryPaymentRepository;
import ir.aut.shop.repository.memory.InMemoryProductRepository;
import ir.aut.shop.repository.memory.InMemoryReviewRepository;
import ir.aut.shop.repository.memory.InMemoryStockChangeLog;
import ir.aut.shop.repository.memory.InMemoryUserRepository;
import ir.aut.shop.service.AccessPolicy;
import ir.aut.shop.service.AddressService;
import ir.aut.shop.service.CartService;
import ir.aut.shop.service.CatalogService;
import ir.aut.shop.service.InventoryService;
import ir.aut.shop.service.OrderService;
import ir.aut.shop.service.PasswordResetService;
import ir.aut.shop.service.PaymentService;
import ir.aut.shop.service.ProductAdminService;
import ir.aut.shop.service.ReviewService;
import ir.aut.shop.service.SimulatedPaymentGateway;
import ir.aut.shop.service.sorting.SortStrategies;
import ir.aut.shop.service.UserService;
import java.util.Scanner;

/**
 * Where the shop starts.
 *
 * <p>This class is the only place that names a storage technology. It picks the in-memory
 * repositories, wires the services and screens over them and hands control to the root
 * menu. Every other class above it is written against the repository interfaces, so moving
 * the shop onto a database would be a change to these eight lines and nowhere else.
 */
public final class Main {

    private static final String DEMO_ADMIN_EMAIL = "admin@shop.ir";
    private static final String DEMO_ADMIN_PASSWORD = "admin123";

    private Main() {
    }

    /**
     * EFFECTS: Starts the shop and runs it until the visitor chooses to leave.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        UserRepository users = new InMemoryUserRepository();
        CategoryRepository categories = new InMemoryCategoryRepository();
        ProductRepository products = new InMemoryProductRepository();
        CartRepository carts = new InMemoryCartRepository();
        OrderRepository orders = new InMemoryOrderRepository();
        PaymentRepository payments = new InMemoryPaymentRepository();
        ReviewRepository reviews = new InMemoryReviewRepository();
        StockChangeLog stockChangeLog = new InMemoryStockChangeLog();

        AccessPolicy access = new AccessPolicy();

        UserService userService = new UserService(users);
        PasswordResetService passwordReset = new PasswordResetService(userService);
        AddressService addressService = new AddressService(users);
        CartService cartService = new CartService(carts, products);
        OrderService orderService = new OrderService(orders, users, products, cartService, access);
        ReviewService reviewService = new ReviewService(reviews, orderService);
        CatalogService catalogService = new CatalogService(products, categories, reviewService,
                        SortStrategies.standard());
        SimulatedPaymentGateway gateway = new SimulatedPaymentGateway();
        PaymentService paymentService = new PaymentService(payments, orderService, gateway);
        InventoryService inventoryService = new InventoryService(products, stockChangeLog, access);
        ProductAdminService productAdminService =
                new ProductAdminService(products, categories, access);

        seedShop(userService, productAdminService);

        ConsoleIo io = new ConsoleIo(new Scanner(System.in));
        Session session = new Session();
        ShopCli shop = new ShopCli(io, session,
                new AuthCli(io, session, userService, passwordReset),
                new ProfileCli(io, session, userService, addressService),
                new CatalogCli(io, session, catalogService, cartService, reviewService),
                new CartCli(io, session, cartService),
                new CheckoutCli(io, session, cartService, addressService, orderService,
                        paymentService),
                new OrderCli(io, session, orderService, paymentService),
                new ReviewCli(io, session, orderService, reviewService),
                new AdminCli(io, session, inventoryService, productAdminService,
                        catalogService, orderService));

        io.print("Demo admin: " + DEMO_ADMIN_EMAIL + " / " + DEMO_ADMIN_PASSWORD);
        shop.run();
    }

    /**
     * MODIFIES: the account, category and product stores.
     * EFFECTS: Puts an admin and a small catalogue in place, so the shop can be walked
     * through without typing everything in first.
     */
    private static void seedShop(UserService userService,
            ProductAdminService productAdminService) {
        User admin = userService.registerAdmin(new Email(DEMO_ADMIN_EMAIL),
                new PhoneNumber("09120000000"), "Shop Manager", DEMO_ADMIN_PASSWORD);

        Category electronics = productAdminService.createCategory(admin, "Electronics");
        Category books = productAdminService.createCategory(admin, "Books");

        productAdminService.createProduct(admin, "Laptop Pro 14",
                "A light laptop with a long battery life.", Money.of("78000000"),
                new StockCount(5), electronics.id(), "Nova");
        productAdminService.createProduct(admin, "Wireless Headphones",
                "Over-ear headphones with noise cancelling.", Money.of("4500000"),
                new StockCount(20), electronics.id(), "Aura");
        productAdminService.createProduct(admin, "Clean Code",
                "Robert C. Martin on writing code people can read.", Money.of("950000"),
                new StockCount(12), books.id(), "Prentice Hall");
        productAdminService.createProduct(admin, "Domain-Driven Design",
                "Eric Evans on modelling the heart of software.", Money.of("1250000"),
                new StockCount(7), books.id(), "Addison-Wesley");
    }
}
