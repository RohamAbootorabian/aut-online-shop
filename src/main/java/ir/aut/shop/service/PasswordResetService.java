package ir.aut.shop.service;

import ir.aut.shop.model.Email;
import ir.aut.shop.model.PasswordHash;
import ir.aut.shop.model.User;
import ir.aut.shop.model.UserId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Recovering a forgotten password (user stories 4 and 5).
 *
 * <p>This used to sit inside {@link UserService}, which therefore had three reasons to
 * change: how accounts are opened, how somebody signs in, and how a reset link is issued
 * and spent. Those are three different things that different pressures move. Issuing a
 * token by email instead of printing it, or giving it an expiry, is a change to recovery
 * alone — and now it is a change to one file alone.
 *
 * <p>The tokens are held here rather than in the account repository on purpose: a reset
 * token is not part of an account, it is a short-lived note about one.
 */
public class PasswordResetService {

    private final UserService users;
    private final Map<String, UserId> issuedTokens = new HashMap<>();

    /**
     * REQUIRES: users is non-null.
     * EFFECTS: Builds the service over the account service.
     *
     * @param users where accounts are looked up and changed
     */
    public PasswordResetService(UserService users) {
        if (users == null) {
            throw new IllegalArgumentException("User service must not be null");
        }
        this.users = users;
    }

    /**
     * REQUIRES: email is non-null.
     * MODIFIES: this service's issued tokens.
     * EFFECTS: Issues a single-use token for resetting the password and returns it, or
     * throws IllegalArgumentException when no account uses that address. User story 4
     * requires the address to be registered already.
     *
     * @param email the address of the account to recover
     * @return the token the customer must present to set a new password
     */
    public String requestReset(Email email) {
        User user = users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
                "No account uses the address " + email));
        String token = UUID.randomUUID().toString();
        issuedTokens.put(token, user.id());
        return token;
    }

    /**
     * REQUIRES: token and newPassword are non-null.
     * MODIFIES: this service's issued tokens, and the matching account.
     * EFFECTS: Replaces the account's password and spends the token, or throws
     * IllegalArgumentException when the token is unknown or already used (user story 5).
     *
     * @param token the token issued by requestReset
     * @param newPassword the new password as typed
     */
    public void completeReset(String token, String newPassword) {
        UserId userId = issuedTokens.remove(token);
        if (userId == null) {
            throw new IllegalArgumentException("Password reset link is unknown or already used");
        }
        users.changePassword(userId, PasswordHash.of(newPassword));
    }
}
