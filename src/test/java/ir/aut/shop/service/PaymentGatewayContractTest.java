package ir.aut.shop.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ir.aut.shop.model.Money;
import ir.aut.shop.model.OrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The six rules {@link PaymentGateway} states, applied to every implementation.
 *
 * <p>The tests are written once, in the abstract class, and run once per implementation.
 * That is what makes substitutability checkable instead of merely claimed: a new provider
 * is added by writing a nested class with one method, and it either passes the same rules
 * the shop already relies on or it does not get used.
 */
abstract class PaymentGatewayContractTest {

    /** The implementation under test. */
    protected abstract PaymentGateway gateway();

    /** An amount this implementation is expected to approve. */
    protected abstract Money acceptableAmount();

    @Test
    @DisplayName("Rule 3: the answer is never null")
    void answerIsNeverNull() {
        assertNotNull(gateway().authorize(OrderId.newId(), acceptableAmount()));
    }

    @Test
    @DisplayName("Rule 3: the reference or reason is never blank")
    void referenceIsNeverBlank() {
        GatewayResult result = gateway().authorize(OrderId.newId(), acceptableAmount());

        assertFalse(result.reference().isBlank());
    }

    @Test
    @DisplayName("Rule 4: a missing argument is an IllegalArgumentException and nothing else")
    void missingArgumentIsRefusedTheSameWay() {
        assertThrows(IllegalArgumentException.class,
                () -> gateway().authorize(null, acceptableAmount()));
        assertThrows(IllegalArgumentException.class,
                () -> gateway().authorize(OrderId.newId(), null));
    }

    @Test
    @DisplayName("Rule 1: any amount may be asked about without the gateway throwing")
    void anyAmountMayBeAskedAbout() {
        assertDoesNotThrow(
                () -> gateway().authorize(OrderId.newId(), Money.of("999000000000")));
        assertDoesNotThrow(() -> gateway().authorize(OrderId.newId(), Money.of("1")));
    }

    @Test
    @DisplayName("Rule 6: asking again after any answer is allowed")
    void askingTwiceIsAllowed() {
        OrderId orderId = OrderId.newId();
        gateway().authorize(orderId, acceptableAmount());

        assertDoesNotThrow(() -> gateway().authorize(orderId, acceptableAmount()));
    }
}
