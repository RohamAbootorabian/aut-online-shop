package ir.aut.shop.repository.memory;

import ir.aut.shop.repository.Repository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A repository that keeps everything in a map for as long as the program runs.
 *
 * <p>The map is insertion-ordered, so walking the whole set gives the same answer twice in
 * a row. That is not promised by the interface, but keeping it steady makes the shop's
 * output predictable to read.
 *
 * @param <I> the identity a stored item is found by
 * @param <T> what is stored
 */
public class InMemoryRepository<I, T> implements Repository<I, T> {

    private final Map<I, T> items = new LinkedHashMap<>();

    @Override
    public void save(I id, T item) {
        if (id == null || item == null) {
            throw new IllegalArgumentException("A repository stores neither a null id nor a null item");
        }
        items.put(id, item);
    }

    @Override
    public Optional<T> findById(I id) {
        return id == null ? Optional.empty() : Optional.ofNullable(items.get(id));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(items.values());
    }

    @Override
    public boolean contains(I id) {
        return id != null && items.containsKey(id);
    }
}
