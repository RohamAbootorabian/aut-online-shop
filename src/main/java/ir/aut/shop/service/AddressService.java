package ir.aut.shop.service;

import ir.aut.shop.model.Address;
import ir.aut.shop.model.AddressId;
import ir.aut.shop.model.PhoneNumber;
import ir.aut.shop.model.PostalCode;
import ir.aut.shop.model.User;
import ir.aut.shop.model.UserId;
import java.util.List;
import java.util.Map;

/**
 * The customer's address book (user story 7).
 *
 * <p>Every operation takes the owner's identity, which is how one customer is kept from
 * touching another's addresses.
 */
public class AddressService {

    private final Map<UserId, User> users;

    /**
     * REQUIRES: users is non-null.
     * EFFECTS: Builds the service over the given account store.
     *
     * @param users where accounts are kept, keyed by identity
     */
    public AddressService(Map<UserId, User> users) {
        if (users == null) {
            throw new IllegalArgumentException("User store must not be null");
        }
        this.users = users;
    }

    /**
     * REQUIRES: every argument is non-null; the text arguments are non-blank.
     * MODIFIES: the owner's address book.
     * EFFECTS: Adds a new address and returns it. The first address a customer adds
     * becomes their default one.
     *
     * @param ownerId whose address book to add to
     * @param recipientName who receives the parcel
     * @param phoneNumber the recipient's contact number
     * @param country the destination country
     * @param city the destination city
     * @param line street, alley and building number
     * @param postalCode the postal code of the destination
     * @param title a short label such as "home" or "office"
     * @return the address that was added
     */
    public Address addAddress(UserId ownerId, String recipientName, PhoneNumber phoneNumber,
            String country, String city, String line, PostalCode postalCode, String title) {
        User owner = requireUser(ownerId);
        Address address = new Address(AddressId.newId(), recipientName, phoneNumber, country,
                city, line, postalCode, title);
        owner.addAddress(address);
        return address;
    }

    /**
     * REQUIRES: every argument is non-null; the text arguments are non-blank.
     * MODIFIES: the named address.
     * EFFECTS: Replaces the address details, or throws IllegalArgumentException when the
     * customer has no such address.
     *
     * @param ownerId whose address book holds the address
     * @param addressId the address to change
     * @param recipientName who receives the parcel
     * @param phoneNumber the recipient's contact number
     * @param country the destination country
     * @param city the destination city
     * @param line street, alley and building number
     * @param postalCode the postal code of the destination
     * @param title a short label such as "home" or "office"
     */
    public void updateAddress(UserId ownerId, AddressId addressId, String recipientName,
            PhoneNumber phoneNumber, String country, String city, String line,
            PostalCode postalCode, String title) {
        Address address = requireAddress(ownerId, addressId);
        address.updateDetails(recipientName, phoneNumber, country, city, line, postalCode,
                title);
    }

    /**
     * REQUIRES: ownerId and addressId are non-null.
     * MODIFIES: the owner's address book.
     * EFFECTS: Drops the address, or throws IllegalArgumentException when the customer has
     * no such address.
     *
     * @param ownerId whose address book to remove from
     * @param addressId the address to drop
     */
    public void removeAddress(UserId ownerId, AddressId addressId) {
        requireUser(ownerId).removeAddress(addressId);
    }

    /**
     * REQUIRES: ownerId and addressId are non-null.
     * MODIFIES: the owner's address book.
     * EFFECTS: Makes the address the default one and clears the flag on the rest, or
     * throws IllegalArgumentException when the customer has no such address.
     *
     * @param ownerId whose address book to change
     * @param addressId the address to make default
     */
    public void markDefault(UserId ownerId, AddressId addressId) {
        requireUser(ownerId).markDefaultAddress(addressId);
    }

    /**
     * REQUIRES: ownerId is non-null.
     * MODIFIES: nothing.
     * EFFECTS: Returns the customer's addresses.
     *
     * @param ownerId whose address book to read
     * @return an unmodifiable list of that customer's addresses
     */
    public List<Address> listAddresses(UserId ownerId) {
        return requireUser(ownerId).addresses();
    }

    private Address requireAddress(UserId ownerId, AddressId addressId) {
        return requireUser(ownerId).findAddress(addressId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No address " + addressId + " for user " + ownerId));
    }

    private User requireUser(UserId userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("No account with id " + userId);
        }
        return user;
    }
}
