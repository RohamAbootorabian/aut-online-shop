package ir.aut.shop.cli;

import ir.aut.shop.model.User;
import java.util.Optional;

/**
 * Who is signed in right now (user stories 2 and 3).
 *
 * <p>This belongs to the presentation layer: the services never assume a current user,
 * they are told whose behalf they act on. Signing out here is what makes the
 * customer-only parts of the menu unreachable, which is exactly what user story 3 asks
 * for.
 */
public class Session {

    private User currentUser;

    /**
     * Reports whether somebody is signed in.
     *
     * @return true when an account is open
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Reports whether the signed-in user may manage the shop.
     *
     * @return true when an admin is signed in
     */
    public boolean isAdminLoggedIn() {
        return isLoggedIn() && currentUser.isAdmin();
    }

    /**
     * Returns who is signed in.
     *
     * @return the current user, or empty when nobody is signed in
     */
    public Optional<User> currentUser() {
        return Optional.ofNullable(currentUser);
    }

    /**
     * REQUIRES: somebody is signed in.
     * EFFECTS: Returns the current user, or throws IllegalStateException when nobody is
     * signed in.
     *
     * @return the current user
     */
    public User requireCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("Nobody is signed in");
        }
        return currentUser;
    }

    /**
     * REQUIRES: user is non-null.
     * MODIFIES: this session.
     * EFFECTS: Opens the account for this run of the program.
     *
     * @param user who just signed in
     */
    public void logIn(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot sign in a null user");
        }
        this.currentUser = user;
    }

    /**
     * MODIFIES: this session.
     * EFFECTS: Closes the account, so the customer-only parts of the shop become
     * unreachable again (user story 3).
     */
    public void logOut() {
        this.currentUser = null;
    }
}
