package ir.aut.shop.repository.memory;

import ir.aut.shop.model.PaymentId;
import ir.aut.shop.model.Payment;
import ir.aut.shop.repository.PaymentRepository;

/**
 * Payment attempts kept in memory.
 */
public class InMemoryPaymentRepository extends InMemoryRepository<PaymentId, Payment> implements PaymentRepository {
}
