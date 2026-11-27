package ir.aut.shop.repository.memory;

import ir.aut.shop.model.CategoryId;
import ir.aut.shop.model.Category;
import ir.aut.shop.repository.CategoryRepository;

/**
 * Categories kept in memory.
 */
public class InMemoryCategoryRepository extends InMemoryRepository<CategoryId, Category> implements CategoryRepository {
}
