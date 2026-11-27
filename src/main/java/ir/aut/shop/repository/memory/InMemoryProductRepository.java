package ir.aut.shop.repository.memory;

import ir.aut.shop.model.ProductId;
import ir.aut.shop.model.Product;
import ir.aut.shop.repository.ProductRepository;

/**
 * Products kept in memory.
 */
public class InMemoryProductRepository extends InMemoryRepository<ProductId, Product> implements ProductRepository {
}
