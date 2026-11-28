package ir.aut.shop.service;

import ir.aut.shop.model.User;

/**
 * Who is allowed to do what.
 *
 * <p>Three services each carried a private <code>requireAdmin</code> of their own: the
 * same rule written three times, with three error messages that could drift apart. Worse
 * than the repetition is what it says about responsibility — deciding who may act is not
 * part of managing stock, and it is not part of managing products either. A service that
 * holds two reasons to change holds one too many.
 *
 * <p>The rule now lives here. If the shop ever grows a second kind of staff, or a rule
 * about accounts that have been suspended, exactly one class learns about it.
 */
public class AccessPolicy {

    /**
     * REQUIRES: nothing.
     * EFFECTS: Builds the policy. It holds no state, but it is an object rather than a
     * static helper so a service depends on something it was handed and a test can put a
     * different policy in its place.
     */
    public AccessPolicy() {
    }

    /**
     * REQUIRES: nothing.
     * MODIFIES: nothing.
     * EFFECTS: Returns quietly when the actor is an admin, and throws
     * IllegalArgumentException otherwise. A missing actor is refused for the same reason
     * as a customer: neither has shown they may act.
     *
     * @param actor whoever is asking to act
     * @param action what they are trying to do, named for the error message
     */
    public void requireAdmin(User actor, String action) {
        if (actor == null || !actor.isAdmin()) {
            throw new IllegalArgumentException("Only an admin may " + action);
        }
    }

    /**
     * REQUIRES: nothing.
     * MODIFIES: nothing.
     * EFFECTS: Reports whether the actor is an admin, without throwing. Screens use this
     * to decide what to show; the throwing form is what actually protects an operation.
     *
     * @param actor whoever is asking
     * @return true when they are an admin
     */
    public boolean isAdmin(User actor) {
        return actor != null && actor.isAdmin();
    }
}
