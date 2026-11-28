package ir.aut.shop.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ir.aut.shop.model.Money;
import ir.aut.shop.model.OrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The stand-in gateway, held to the contract every implementation must keep.
 */
@DisplayName("SimulatedPaymentGateway keeps the PaymentGateway contract")
class SimulatedPaymentGatewayContractTest extends PaymentGatewayContractTest {

    private final SimulatedPaymentGateway subject = new SimulatedPaymentGateway();

    @Override
    protected PaymentGateway gateway() {
        return subject;
    }

    @Override
    protected Money acceptableAmount() {
        return Money.of("950000");
    }

    @Test
    @DisplayName("Rule 2: a refusing gateway answers, it does not throw")
    void refusalIsAnAnswer() {
        subject.refuseEverything("Insufficient funds");

        GatewayResult result = subject.authorize(OrderId.newId(), acceptableAmount());

        assertFalse(result.approved());
        assertTrue(result.reference().contains("Insufficient funds"));
    }
}
