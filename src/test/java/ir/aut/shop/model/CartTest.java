package ir.aut.shop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the cart against its own contract rather than its insides.
 *
 * <p>Every assertion below is written in terms of the published operations, so swapping
 * the representation from a map to a list would leave this test untouched.
 */
class CartTest {

    private Cart cart;
    private Product book;
    private Product laptop;

    @BeforeEach
    void setUp() {
        cart = new Cart(UserId.newId());
        book = productNamed("Clean Code", "950000", 12);
        laptop = productNamed("Laptop Pro 14", "78000000", 5);
    }

    @Test
    @DisplayName("A new cart holds nothing and costs nothing")
    void newCartIsEmpty() {
        assertTrue(cart.isEmpty());
        assertEquals(Money.ZERO, cart.itemsSubtotal());
        assertEquals(0, cart.quantityOf(book.id()));
    }

    @Test
    @DisplayName("Story 15: adding the same product again adds the quantities up")
    void addingTheSameProductTwiceSumsQuantities() {
        cart.addProduct(book, new Quantity(2));
        cart.addProduct(book, new Quantity(3));

        assertEquals(5, cart.quantityOf(book.id()));
        assertEquals(1, cart.lines().size(), "the same product took two lines");
    }

    @Test
    @DisplayName("A line keeps the price captured when the product was added")
    void lineKeepsTheCapturedPrice() {
        cart.addProduct(book, new Quantity(2));
        book.changePrice(Money.of("5000000"));

        assertEquals(Money.of("1900000"), cart.itemsSubtotal(),
                "a later price change rewrote the cart");
    }

    @Test
    @DisplayName("Story 16: the subtotal is the sum of the line totals")
    void subtotalAddsUpTheLines() {
        cart.addProduct(book, new Quantity(2));
        cart.addProduct(laptop, new Quantity(1));

        assertEquals(Money.of("79900000"), cart.itemsSubtotal());
    }

    @Test
    @DisplayName("Story 17: changing a quantity replaces it rather than adding to it")
    void changingQuantityReplacesIt() {
        cart.addProduct(book, new Quantity(2));
        cart.changeQuantity(book.id(), new Quantity(7));

        assertEquals(7, cart.quantityOf(book.id()));
    }

    @Test
    @DisplayName("Changing the quantity of something not in the cart is refused")
    void changingAnAbsentProductIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> cart.changeQuantity(book.id(), new Quantity(1)));
    }

    @Test
    @DisplayName("Story 18: removing something the cart does not hold changes nothing")
    void removingAnAbsentProductChangesNothing() {
        cart.addProduct(book, new Quantity(2));
        cart.removeProduct(laptop.id());

        assertEquals(2, cart.quantityOf(book.id()));
        assertEquals(1, cart.lines().size());
    }

    @Test
    @DisplayName("Story 19: emptying the cart leaves nothing and a zero total")
    void clearingEmptiesTheCart() {
        cart.addProduct(book, new Quantity(2));
        cart.addProduct(laptop, new Quantity(1));
        cart.clear();

        assertTrue(cart.isEmpty());
        assertEquals(Money.ZERO, cart.itemsSubtotal());
    }

    @Test
    @DisplayName("The returned lines cannot be used to reach inside the cart")
    void linesDoNotExposeTheRepresentation() {
        cart.addProduct(book, new Quantity(2));
        List<CartLine> firstLook = cart.lines();

        assertThrows(UnsupportedOperationException.class, firstLook::clear);

        cart.addProduct(laptop, new Quantity(1));
        assertEquals(1, firstLook.size(), "an earlier list changed with the cart");
    }

    private static Product productNamed(String name, String price, int stock) {
        return new Product(ProductId.newId(), name, "A product.", Money.of(price),
                new StockCount(stock), CategoryId.newId(), "Test Brand",
                LocalDateTime.now());
    }
}
