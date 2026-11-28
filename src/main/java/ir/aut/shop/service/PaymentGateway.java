package ir.aut.shop.service;

import ir.aut.shop.model.Money;
import ir.aut.shop.model.OrderId;

/**
 * The outside party that actually moves the money (user stories 10 and 11 model it as an
 * actor of its own).
 *
 * <p>This interface is the boundary the shop hides behind. Which provider is used, how it
 * is reached and what it charges are decisions that may change; nothing above this line
 * depends on them.
 *
 * <h2>The contract every implementation must keep</h2>
 *
 * <p>An interface is only useful if one implementation can stand in for another without
 * the caller noticing. That is not something a compiler can check, so it is written down
 * here and checked by a test every implementation must pass:
 *
 * <ol>
 *   <li><b>No implementation may demand more than this one does.</b> Any order and any
 *       amount must be accepted as a question. An implementation that refused to be asked
 *       about, say, large amounts — by throwing — would break every caller written
 *       against this interface.</li>
 *   <li><b>Refusing is an answer, not an error.</b> A gateway that will not take the money
 *       returns a refusing {@link GatewayResult} carrying a reason. It does not throw.</li>
 *   <li><b>The answer is never null</b>, and its reference or reason is never blank.</li>
 *   <li><b>Only a missing argument is an error</b>, and it is an
 *       {@link IllegalArgumentException}. No implementation may invent a new kind of
 *       failure for the caller to learn about.</li>
 *   <li><b>Nothing inside the shop changes.</b> Recording what happened is the payment
 *       service's job, not the gateway's.</li>
 *   <li><b>A refusal is not final.</b> Asking again after a refusal must be allowed, which
 *       is what lets user story 24 retry a failed payment.</li>
 * </ol>
 *
 * <p>Those six rules are the substitution principle stated for this one boundary. A new
 * provider is a drop-in replacement exactly as far as it keeps them, and
 * <code>PaymentGatewayContractTest</code> is where it is held to them.
 */
public interface PaymentGateway {

    /**
     * REQUIRES: orderId and amount are non-null.
     * MODIFIES: nothing inside the shop.
     * EFFECTS: Asks the gateway to take the amount for the order and returns its answer,
     * approving or refusing. Throws IllegalArgumentException when either argument is
     * missing, and nothing else: a gateway that will not take the money says so in the
     * result rather than by throwing.
     *
     * @param orderId the order being paid for
     * @param amount how much to take
     * @return the gateway's answer, approving or refusing
     */
    GatewayResult authorize(OrderId orderId, Money amount);
}
