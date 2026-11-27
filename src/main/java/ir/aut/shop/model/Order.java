package ir.aut.shop.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A placed order (user stories 20 to 27).
 *
 * <p>An order is a historical document, so its lines, its shipping address and its three
 * money figures are fixed at the moment it is placed and never change afterwards. Only
 * {@link #status()} moves, and only through {@link #markPaid()} and {@link #cancel()},
 * because user stories 23 and 24 need an order to go from awaiting payment to paid or
 * cancelled.
 *
 * <p>The shipping address is a frozen copy rather than a reference into the customer's
 * address book. Editing an address later must not rewrite where a past parcel was sent.
 *
 * <p>The customer never supplies a total: every figure here is worked out from the lines
 * (user stories 20 and 22).
 */
public class Order {

    private final OrderId id;
    private final UserId customerId;
    private final List<OrderLine> lines;
    private final Address shippingAddress;
    private final Money itemsSubtotal;
    private final Money shippingFee;
    private final Money totalAmount;
    private final LocalDateTime placedAt;
    private OrderStatus status;

    /**
     * REQUIRES: every argument is non-null and lines is not empty.
     * EFFECTS: Builds an order awaiting payment whose subtotal is the sum of its line
     * totals and whose total is that subtotal plus the shipping fee. Throws
     * IllegalArgumentException when an argument is missing or there are no lines.
     *
     * @param id the identity of this order
     * @param customerId the customer who placed it
     * @param lines what was ordered; copied in, so later edits to the list are ignored
     * @param shippingAddress where the parcel goes; copied in
     * @param shippingFee what delivery costs
     * @param placedAt when the order was placed
     */
    public Order(OrderId id, UserId customerId, List<OrderLine> lines, Address shippingAddress,
            Money shippingFee, LocalDateTime placedAt) {
        if (id == null || customerId == null || shippingAddress == null || shippingFee == null
                || placedAt == null) {
            throw new IllegalArgumentException("Order is missing a required field");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one line");
        }
        this.id = id;
        this.customerId = customerId;
        this.lines = List.copyOf(lines);
        this.shippingAddress = shippingAddress.frozenCopy();
        this.shippingFee = shippingFee;
        this.itemsSubtotal = this.lines.stream()
                .map(OrderLine::lineTotal)
                .reduce(Money.ZERO, Money::plus);
        this.totalAmount = this.itemsSubtotal.plus(shippingFee);
        this.placedAt = placedAt;
        this.status = OrderStatus.AWAITING_PAYMENT;
        checkRep();
    }

    public OrderId id() {
        return id;
    }

    public UserId customerId() {
        return customerId;
    }

    /**
     * Returns what was ordered (user story 27).
     *
     * @return an unmodifiable list of the order's lines
     */
    public List<OrderLine> lines() {
        return lines;
    }

    /**
     * Returns where the parcel goes, as frozen when the order was placed.
     *
     * @return the shipping address
     */
    public Address shippingAddress() {
        return shippingAddress;
    }

    public Money itemsSubtotal() {
        return itemsSubtotal;
    }

    public Money shippingFee() {
        return shippingFee;
    }

    /**
     * Returns what the customer must pay (user story 22).
     *
     * @return the item subtotal plus the shipping fee
     */
    public Money totalAmount() {
        return totalAmount;
    }

    public LocalDateTime placedAt() {
        return placedAt;
    }

    public OrderStatus status() {
        return status;
    }

    /**
     * Reports whether this order may still be paid for.
     *
     * <p>This is what stops a paid order being paid twice (user story 23) and what lets a
     * customer retry after a refused payment (user story 24).
     *
     * @return true when the order is still awaiting payment
     */
    public boolean isPayable() {
        return status == OrderStatus.AWAITING_PAYMENT;
    }

    /**
     * Reports whether this order has been paid for.
     *
     * @return true when the payment was confirmed
     */
    public boolean isPaid() {
        return status == OrderStatus.PAID;
    }

    /**
     * REQUIRES: this order is payable.
     * MODIFIES: this order.
     * EFFECTS: Moves the order to paid, or throws IllegalStateException when it was
     * already paid or cancelled. Nothing else about the order changes.
     */
    public void markPaid() {
        if (!isPayable()) {
            throw new IllegalStateException("Order " + id + " is already " + status);
        }
        this.status = OrderStatus.PAID;
        checkRep();
    }

    /**
     * REQUIRES: this order is payable.
     * MODIFIES: this order.
     * EFFECTS: Moves the order to cancelled so it can no longer be paid, or throws
     * IllegalStateException when it was already paid or cancelled.
     */
    public void cancel() {
        if (!isPayable()) {
            throw new IllegalStateException("Order " + id + " is already " + status);
        }
        this.status = OrderStatus.CANCELLED;
        checkRep();
    }

    // RI: every field is non-null; there is at least one line;
    //     itemsSubtotal is the sum of the line totals;
    //     totalAmount is itemsSubtotal plus shippingFee.
    private void checkRep() {
        assert id != null : "order id is null";
        assert customerId != null : "order customer is null";
        assert shippingAddress != null : "order shipping address is null";
        assert status != null : "order status is null";
        assert placedAt != null : "order placed-at is null";
        assert !lines.isEmpty() : "order has no lines";
        Money expectedSubtotal = lines.stream()
                .map(OrderLine::lineTotal)
                .reduce(Money.ZERO, Money::plus);
        assert itemsSubtotal.equals(expectedSubtotal) : "subtotal does not match the lines";
        assert totalAmount.equals(itemsSubtotal.plus(shippingFee))
                : "total does not match subtotal plus shipping";
    }

    @Override
    public String toString() {
        return "Order " + id + " (" + status + ", " + totalAmount + ")";
    }
}
