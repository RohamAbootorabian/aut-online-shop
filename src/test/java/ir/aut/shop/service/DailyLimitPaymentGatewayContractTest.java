package ir.aut.shop.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ir.aut.shop.model.Money;
import ir.aut.shop.model.OrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A second implementation, held to exactly the same contract.
 *
 * <p>Nothing in this file is written twice: the six rules come from the parent, and only
 * what is particular to a ceiling is added here. That is the whole point — a new provider
 * costs one small class, and the shop above it is not touched.
 */
@DisplayName("DailyLimitPaymentGateway keeps the PaymentGateway contract")
class DailyLimitPaymentGatewayContractTest extends PaymentGatewayContractTest {

    private final DailyLimitPaymentGateway subject =
            new DailyLimitPaymentGateway(Money.of("5000000"));

    @Override
    protected PaymentGateway gateway() {
        return subject;
    }

    @Override
    protected Money acceptableAmount() {
        return Money.of("950000");
    }

    @Test
    @DisplayName("Rule 2: an amount over the ceiling is refused, not thrown about")
    void overTheCeilingIsRefusedNotThrown() {
        GatewayResult result = subject.authorize(OrderId.newId(), Money.of("78000000"));

        assertFalse(result.approved());
        assertTrue(result.reference().contains("will not take more than"));
    }

    @Test
    @DisplayName("The ceiling itself is still approved")
    void theCeilingIsInclusive() {
        assertTrue(subject.authorize(OrderId.newId(), subject.ceiling()).approved());
    }
}
