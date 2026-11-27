package ir.aut.shop.repository;

import java.util.List;
import java.util.Optional;

/**
 * Where a kind of thing is kept, said without saying where.
 *
 * <p>A service that stores accounts needs to put one away, fetch one back and walk the
 * whole set. It does not need to know whether that happens in a map, a file or a database.
 * This interface is exactly those needs and nothing more, so the storage decision can
 * change without a single service changing with it.
 *
 * @param <I> the identity a stored item is found by
 * @param <T> what is stored
 */
public interface Repository<I, T> {

    /**
     * REQUIRES: id and item are non-null.
     * MODIFIES: this repository.
     * EFFECTS: Stores the item under the identity, replacing whatever was there before.
     *
     * @param id the identity to store under
     * @param item the item to store
     */
    void save(I id, T item);

    /**
     * REQUIRES: nothing.
     * MODIFIES: nothing.
     * EFFECTS: Returns the item stored under the identity, or empty when there is none.
     * A null identity gives an empty answer rather than an error, because asking about
     * nothing is not a mistake.
     *
     * @param id the identity to look for
     * @return the item, if one is stored
     */
    Optional<T> findById(I id);

    /**
     * REQUIRES: nothing.
     * MODIFIES: nothing.
     * EFFECTS: Returns every stored item, in no promised order. The list is the caller's
     * own; changing it must not change what the repository holds.
     *
     * @return all stored items
     */
    List<T> findAll();

    /**
     * REQUIRES: nothing.
     * MODIFIES: nothing.
     * EFFECTS: Reports whether something is stored under the identity.
     *
     * @param id the identity to look for
     * @return true when an item is stored under it
     */
    boolean contains(I id);
}
