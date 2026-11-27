package ir.aut.shop.repository;

import ir.aut.shop.model.StockChange;
import java.util.List;

/**
 * Where every change to a product's stock is recorded.
 *
 * <p>This is deliberately not a {@link Repository}. A stock change is never looked up by
 * identity, never replaced and never removed: the log only ever grows, and it is only ever
 * read as a whole. Giving it the four methods of a repository would hand the inventory
 * service a <code>save</code> that overwrites history and a <code>contains</code> that
 * means nothing. An interface says what a caller may do, so this one says only what a log
 * allows.
 */
public interface StockChangeLog {

    /**
     * REQUIRES: change is non-null.
     * MODIFIES: this log.
     * EFFECTS: Adds the change to the end of the log. Nothing already recorded changes.
     *
     * @param change what happened to the stock
     */
    void append(StockChange change);

    /**
     * REQUIRES: nothing.
     * MODIFIES: nothing.
     * EFFECTS: Returns every recorded change, oldest first. The list is the caller's own;
     * changing it must not change the log.
     *
     * @return the whole history
     */
    List<StockChange> findAll();
}
