package ir.aut.shop.service;

import ir.aut.shop.model.Email;
import ir.aut.shop.model.PasswordHash;
import ir.aut.shop.model.PhoneNumber;
import ir.aut.shop.model.User;
import ir.aut.shop.model.UserId;
import ir.aut.shop.model.UserRole;
import ir.aut.shop.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Opening an account, signing in and recovering a password (user stories 1 to 6).
 *
 * <p>Accounts live in a repository handed in from outside. This class never names a
 * storage technology, so swapping the in-memory store for a database is a change to
 * {@link ir.aut.shop.Main} alone.
 */
public class UserService {

    private final UserRepository users;
    private final Map<String, UserId> resetTokens = new HashMap<>();

    /**
     * REQUIRES: users is non-null.
     * EFFECTS: Builds the service over the given account store.
     *
     * @param users where accounts are kept
     */
    public UserService(UserRepository users) {
        if (users == null) {
            throw new IllegalArgumentException("User repository must not be null");
        }
        this.users = users;
    }

    /**
     * REQUIRES: every argument is non-null; fullName is non-blank.
     * MODIFIES: the account store.
     * EFFECTS: Opens a customer account and returns it, or throws IllegalArgumentException
     * when the email or the phone number already belongs to someone (user story 1). The
     * password is stored hashed, never as typed.
     *
     * @param email the address the customer will sign in with
     * @param phoneNumber the mobile number the customer will sign in with
     * @param fullName the name to show on the profile
     * @param rawPassword the password as typed
     * @return the account that was opened
     */
    public User signUp(Email email, PhoneNumber phoneNumber, String fullName,
            String rawPassword) {
        requireFreeEmail(email);
        requireFreePhoneNumber(phoneNumber);
        User user = new User(UserId.newId(), email, phoneNumber, fullName,
                PasswordHash.of(rawPassword), UserRole.CUSTOMER, LocalDateTime.now());
        users.save(user.id(), user);
        return user;
    }

    /**
     * REQUIRES: every argument is non-null; fullName is non-blank.
     * MODIFIES: the account store.
     * EFFECTS: Opens an admin account and returns it. Used to seed the shop with the staff
     * that user stories 28 to 36 assume already exist.
     *
     * @param email the address the admin will sign in with
     * @param phoneNumber the admin's mobile number
     * @param fullName the admin's name
     * @param rawPassword the password as typed
     * @return the account that was opened
     */
    public User registerAdmin(Email email, PhoneNumber phoneNumber, String fullName,
            String rawPassword) {
        requireFreeEmail(email);
        requireFreePhoneNumber(phoneNumber);
        User admin = new User(UserId.newId(), email, phoneNumber, fullName,
                PasswordHash.of(rawPassword), UserRole.ADMIN, LocalDateTime.now());
        users.save(admin.id(), admin);
        return admin;
    }

    /**
     * REQUIRES: nothing.
     * MODIFIES: nothing.
     * EFFECTS: Returns the account matching the credentials, or throws
     * IllegalArgumentException when either is blank or the pair does not match an account
     * (user story 2). The same message is used for an unknown account and a wrong
     * password, so the answer does not reveal which accounts exist.
     *
     * @param emailOrPhone the email address or mobile number typed at login
     * @param rawPassword the password as typed
     * @return the account that was opened
     */
    public User logIn(String emailOrPhone, String rawPassword) {
        if (emailOrPhone == null || emailOrPhone.isBlank()) {
            throw new IllegalArgumentException("Email or phone number must not be blank");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        return findByEmailOrPhone(emailOrPhone)
                .filter(user -> user.matchesPassword(rawPassword))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No account matches those credentials"));
    }

    /**
     * REQUIRES: email is non-null.
     * MODIFIES: the reset-token store.
     * EFFECTS: Issues a single-use token for resetting the password and returns it, or
     * throws IllegalArgumentException when no account uses that address. User story 4
     * requires the address to be registered already.
     *
     * @param email the address of the account to recover
     * @return the token the customer must present to set a new password
     */
    public String requestPasswordReset(Email email) {
        User user = findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
                "No account uses the address " + email));
        String token = UUID.randomUUID().toString();
        resetTokens.put(token, user.id());
        return token;
    }

    /**
     * REQUIRES: token and newPassword are non-null.
     * MODIFIES: the matching account and the reset-token store.
     * EFFECTS: Replaces the account's password and spends the token, or throws
     * IllegalArgumentException when the token is unknown or already used.
     *
     * @param token the token issued by requestPasswordReset
     * @param newPassword the new password as typed
     */
    public void resetPassword(String token, String newPassword) {
        UserId userId = resetTokens.remove(token);
        if (userId == null) {
            throw new IllegalArgumentException("Password reset link is unknown or already used");
        }
        User user = requireUser(userId);
        user.changePassword(PasswordHash.of(newPassword));
    }

    /**
     * REQUIRES: userId is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the account with that identity, or empty when there is none
     * (user story 5).
     *
     * @param userId the account to look up
     * @return the account, or empty
     */
    public Optional<User> findById(UserId userId) {
        return users.findById(userId);
    }

    /**
     * REQUIRES: every argument is non-null; fullName is non-blank.
     * MODIFIES: the matching account.
     * EFFECTS: Replaces the editable profile details and returns the account, or throws
     * IllegalArgumentException when the account is unknown or the new email or phone
     * number already belongs to someone else (user story 6).
     *
     * @param userId whose profile to change
     * @param fullName the new display name
     * @param email the new email address
     * @param phoneNumber the new mobile number
     * @return the updated account
     */
    public User updateProfile(UserId userId, String fullName, Email email,
            PhoneNumber phoneNumber) {
        User user = requireUser(userId);
        findByEmail(email)
                .filter(other -> !other.id().equals(userId))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Email already in use: " + email);
                });
        findByPhoneNumber(phoneNumber)
                .filter(other -> !other.id().equals(userId))
                .ifPresent(other -> {
                    throw new IllegalArgumentException(
                            "Phone number already in use: " + phoneNumber);
                });
        user.updateProfile(fullName, email, phoneNumber);
        return user;
    }

    private Optional<User> findByEmailOrPhone(String emailOrPhone) {
        String typed = emailOrPhone.trim();
        return users.findAll().stream()
                .filter(user -> user.email().value().equalsIgnoreCase(typed)
                        || user.phoneNumber().value().equals(typed))
                .findFirst();
    }

    private Optional<User> findByEmail(Email email) {
        return users.findAll().stream()
                .filter(user -> user.email().equals(email))
                .findFirst();
    }

    private Optional<User> findByPhoneNumber(PhoneNumber phoneNumber) {
        return users.findAll().stream()
                .filter(user -> user.phoneNumber().equals(phoneNumber))
                .findFirst();
    }

    private void requireFreeEmail(Email email) {
        if (findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
    }

    private void requireFreePhoneNumber(PhoneNumber phoneNumber) {
        if (findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalArgumentException(
                    "Phone number already registered: " + phoneNumber);
        }
    }

    private User requireUser(UserId userId) {
        return users.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("No account with id " + userId));
    }
}
