package ir.aut.shop.repository;

import ir.aut.shop.model.PaymentId;
import ir.aut.shop.model.Payment;

/**
 * Where payment attempts are kept.
 */
public interface PaymentRepository extends Repository<PaymentId, Payment> {
}
