package ir.aut.shop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Someone with an account in the shop.
 *
 * <p>Identity is the account, not the details: the email, phone and name all change over
 * time while the same person keeps the same {@link UserId} (user story 6).
 *
 * <p>The stored password is never readable. There is no getter for it; the only way to ask
 * about it is {@link #matchesPassword(String)}, which answers yes or no. That is what lets
 * user story 5 promise the password is never displayed.
 */
public class User {

    private final UserId id;
    private final UserRole role;
    private final LocalDateTime createdAt;
    private final List<Address> addresses;
    private Email email;
    private PhoneNumber phoneNumber;
    private String fullName;
    private PasswordHash passwordHash;

    /**
     * REQUIRES: every argument is non-null and fullName is non-blank.
     * EFFECTS: Builds an account with an empty address book, or throws
     * IllegalArgumentException when an argument is missing.
     *
     * @param id the identity of this account
     * @param email the address used to sign in and to reach the user
     * @param phoneNumber the mobile number used to sign in and to reach the user
     * @param fullName the name shown on the profile
     * @param passwordHash the stored form of the chosen password
     * @param role what this user is allowed to do
     * @param createdAt when the account was opened
     */
    public User(UserId id, Email email, PhoneNumber phoneNumber, String fullName,
            PasswordHash passwordHash, UserRole role, LocalDateTime createdAt) {
        if (id == null || email == null || phoneNumber == null || passwordHash == null
                || role == null || createdAt == null) {
            throw new IllegalArgumentException("User is missing a required field");
        }
        this.id = id;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.fullName = requireFullName(fullName);
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
        this.addresses = new ArrayList<>();
        checkRep();
    }

    public UserId id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public PhoneNumber phoneNumber() {
        return phoneNumber;
    }

    public String fullName() {
        return fullName;
    }

    public UserRole role() {
        return role;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    /**
     * Reports whether this user may manage products, stock and every order.
     *
     * @return true when the user is an admin
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /**
     * Reports whether a typed password opens this account (user story 2).
     *
     * @param rawPassword the password as typed at login
     * @return true when the password matches the stored one
     */
    public boolean matchesPassword(String rawPassword) {
        return passwordHash.matches(rawPassword);
    }

    /**
     * Returns this user's address book (user story 7).
     *
     * @return an unmodifiable copy; later changes to the book leave it alone
     */
    public List<Address> addresses() {
        return List.copyOf(addresses);
    }

    /**
     * Finds the address used unless the customer picks another.
     *
     * @return the default address, or empty when none is marked
     */
    public Optional<Address> findDefaultAddress() {
        return addresses.stream()
                .filter(Address::isDefault)
                .findFirst();
    }

    /**
     * Finds one address by its identity.
     *
     * @param addressId the address to look for
     * @return the address, or empty when this user has no such address
     */
    public Optional<Address> findAddress(AddressId addressId) {
        return addresses.stream()
                .filter(address -> address.id().equals(addressId))
                .findFirst();
    }

    /**
     * REQUIRES: newPasswordHash is non-null.
     * MODIFIES: this user.
     * EFFECTS: Replaces the stored password (user story 4).
     *
     * @param newPasswordHash the stored form of the new password
     */
    public void changePassword(PasswordHash newPasswordHash) {
        if (newPasswordHash == null) {
            throw new IllegalArgumentException("Password hash must not be null");
        }
        this.passwordHash = newPasswordHash;
        checkRep();
    }

    /**
     * REQUIRES: every argument is non-null and fullName is non-blank.
     * MODIFIES: this user.
     * EFFECTS: Replaces the editable profile details (user story 6).
     *
     * @param fullName the new display name
     * @param email the new email address
     * @param phoneNumber the new mobile number
     */
    public void updateProfile(String fullName, Email email, PhoneNumber phoneNumber) {
        if (email == null || phoneNumber == null) {
            throw new IllegalArgumentException("Profile email and phone must not be null");
        }
        this.fullName = requireFullName(fullName);
        this.email = email;
        this.phoneNumber = phoneNumber;
        checkRep();
    }

    /**
     * REQUIRES: address is non-null and its id is not already in the book.
     * MODIFIES: this user.
     * EFFECTS: Adds the address. The first address added becomes the default one, so a
     * customer always has somewhere to ship to.
     *
     * @param address the address to add
     */
    public void addAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address must not be null");
        }
        if (findAddress(address.id()).isPresent()) {
            throw new IllegalArgumentException("Address already exists: " + address.id());
        }
        if (addresses.isEmpty()) {
            address.markDefault();
        }
        addresses.add(address);
        checkRep();
    }

    /**
     * REQUIRES: the address belongs to this user.
     * MODIFIES: this user.
     * EFFECTS: Drops the address, or throws IllegalArgumentException when this user has no
     * such address. If the removed address was the default one, the first remaining
     * address takes over so the book is never left without a default.
     *
     * @param addressId the address to drop
     */
    public void removeAddress(AddressId addressId) {
        Address address = requireOwnAddress(addressId);
        addresses.remove(address);
        if (address.isDefault() && !addresses.isEmpty()) {
            addresses.get(0).markDefault();
        }
        checkRep();
    }

    /**
     * REQUIRES: the address belongs to this user.
     * MODIFIES: this user.
     * EFFECTS: Makes the address the default one and clears the flag on every other
     * address, or throws IllegalArgumentException when this user has no such address.
     *
     * @param addressId the address to make default
     */
    public void markDefaultAddress(AddressId addressId) {
        Address chosen = requireOwnAddress(addressId);
        addresses.forEach(Address::unmarkDefault);
        chosen.markDefault();
        checkRep();
    }

    private Address requireOwnAddress(AddressId addressId) {
        return findAddress(addressId).orElseThrow(() -> new IllegalArgumentException(
                "User " + id + " has no address " + addressId));
    }

    private static String requireFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name must not be blank");
        }
        return fullName.trim();
    }

    // RI: id, email, phoneNumber, passwordHash, role and createdAt are non-null;
    //     fullName is non-blank; no two addresses share an id;
    //     at most one address is marked default;
    //     a non-empty address book always has exactly one default.
    private void checkRep() {
        assert id != null : "user id is null";
        assert email != null : "user email is null";
        assert phoneNumber != null : "user phone number is null";
        assert passwordHash != null : "user password hash is null";
        assert role != null : "user role is null";
        assert createdAt != null : "user created-at is null";
        assert fullName != null && !fullName.isBlank() : "user full name is blank";
        assert addresses.stream().map(Address::id).distinct().count() == addresses.size()
                : "duplicate address id in address book";
        long defaults = addresses.stream().filter(Address::isDefault).count();
        assert addresses.isEmpty() ? defaults == 0 : defaults == 1
                : "address book must have exactly one default address";
    }

    @Override
    public String toString() {
        return fullName + " <" + email + ">";
    }
}
