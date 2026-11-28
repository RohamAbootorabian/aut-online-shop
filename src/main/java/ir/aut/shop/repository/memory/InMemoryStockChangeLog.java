package ir.aut.shop.repository.memory;

import ir.aut.shop.model.StockChange;
import ir.aut.shop.repository.StockChangeLog;
import java.util.ArrayList;
import java.util.List;

/**
 * The stock history kept in memory, in the order it happened.
 */
public class InMemoryStockChangeLog implements StockChangeLog {

    private final List<StockChange> changes = new ArrayList<>();

    @Override
    public void append(StockChange change) {
        if (change == null) {
            throw new IllegalArgumentException("A stock change must not be null");
        }
        changes.add(change);
    }

    @Override
    public List<StockChange> findAll() {
        return List.copyOf(changes);
    }
}
