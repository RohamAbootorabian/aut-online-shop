package ir.aut.shop.repository;

import ir.aut.shop.model.ProductId;
import ir.aut.shop.model.Product;

/**
 * Where products are kept.
 */
public interface ProductRepository extends Repository<ProductId, Product> {
}
