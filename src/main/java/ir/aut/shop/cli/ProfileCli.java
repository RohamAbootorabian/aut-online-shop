package ir.aut.shop.cli;

import ir.aut.shop.model.Address;
import ir.aut.shop.model.Email;
import ir.aut.shop.model.PhoneNumber;
import ir.aut.shop.model.PostalCode;
import ir.aut.shop.model.User;
import ir.aut.shop.service.AddressService;
import ir.aut.shop.service.UserService;
import java.util.List;
import java.util.Optional;

/**
 * Viewing and editing the profile, and keeping the address book
 * (user stories 5 to 7).
 */
public class ProfileCli {

    private static final int VIEW_PROFILE = 1;
    private static final int EDIT_PROFILE = 2;
    private static final int LIST_ADDRESSES = 3;
    private static final int ADD_ADDRESS = 4;
    private static final int EDIT_ADDRESS = 5;
    private static final int REMOVE_ADDRESS = 6;
    private static final int MARK_DEFAULT_ADDRESS = 7;
    private static final int BACK = 0;

    private final ConsoleIo io;
    private final Session session;
    private final UserService userService;
    private final AddressService addressService;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds the screen over the given console, session and services.
     *
     * @param io where questions are asked and answers shown
     * @param session who is signed in
     * @param userService the accounts behind the screen
     * @param addressService the address books behind the screen
     */
    public ProfileCli(ConsoleIo io, Session session, UserService userService,
            AddressService addressService) {
        this.io = io;
        this.session = session;
        this.userService = userService;
        this.addressService = addressService;
    }

    /**
     * MODIFIES: the signed-in account.
     * EFFECTS: Runs the profile menu until the customer goes back.
     */
    public void run() {
        while (true) {
            io.printTitle("My profile");
            io.print("  1) View profile");
            io.print("  2) Edit profile");
            io.print("  3) My addresses");
            io.print("  4) Add an address");
            io.print("  5) Edit an address");
            io.print("  6) Remove an address");
            io.print("  7) Set the default address");
            io.print("  0) Back");

            int choice = io.readInt("Choose", BACK, MARK_DEFAULT_ADDRESS);
            try {
                switch (choice) {
                    case VIEW_PROFILE -> showProfile();
                    case EDIT_PROFILE -> editProfile();
                    case LIST_ADDRESSES -> listAddresses();
                    case ADD_ADDRESS -> addAddress();
                    case EDIT_ADDRESS -> editAddress();
                    case REMOVE_ADDRESS -> removeAddress();
                    case MARK_DEFAULT_ADDRESS -> markDefaultAddress();
                    default -> {
                        return;
                    }
                }
            } catch (IllegalArgumentException problem) {
                io.printError(problem.getMessage());
            }
        }
    }

    /** EFFECTS: Shows the profile. The password is never among the shown fields. */
    private void showProfile() {
        User user = session.requireCurrentUser();
        io.printTitle("Profile");
        io.print("Name   : " + user.fullName());
        io.print("Email  : " + user.email());
        io.print("Mobile : " + user.phoneNumber());
        io.print("Role   : " + user.role());
        io.print("Joined : " + user.createdAt().toLocalDate());
    }

    private void editProfile() {
        User user = session.requireCurrentUser();
        io.printTitle("Edit profile");
        String fullName = io.readLine("Full name [" + user.fullName() + "]");
        Email email = new Email(io.readLine("Email [" + user.email() + "]"));
        PhoneNumber phoneNumber =
                new PhoneNumber(io.readLine("Mobile [" + user.phoneNumber() + "]"));

        userService.updateProfile(user.id(), fullName, email, phoneNumber);
        io.printSuccess("Your profile has been updated.");
    }

    private void listAddresses() {
        io.printTitle("My addresses");
        io.printNumberedList(addressLabels(), "You have no addresses yet.");
    }

    private void addAddress() {
        User user = session.requireCurrentUser();
        io.printTitle("Add an address");
        addressService.addAddress(user.id(),
                io.readLine("Recipient name"),
                new PhoneNumber(io.readLine("Recipient mobile")),
                io.readLine("Country"),
                io.readLine("City"),
                io.readLine("Street, alley and number"),
                new PostalCode(io.readLine("Postal code (10 digits)")),
                io.readLine("Label, such as home or office"));
        io.printSuccess("Address saved.");
    }

    private void editAddress() {
        User user = session.requireCurrentUser();
        Optional<Address> chosen = chooseAddress("Which address to edit");
        if (chosen.isEmpty()) {
            return;
        }
        io.printTitle("Edit address");
        addressService.updateAddress(user.id(), chosen.get().id(),
                io.readLine("Recipient name"),
                new PhoneNumber(io.readLine("Recipient mobile")),
                io.readLine("Country"),
                io.readLine("City"),
                io.readLine("Street, alley and number"),
                new PostalCode(io.readLine("Postal code (10 digits)")),
                io.readLine("Label"));
        io.printSuccess("Address updated.");
    }

    private void removeAddress() {
        User user = session.requireCurrentUser();
        Optional<Address> chosen = chooseAddress("Which address to remove");
        if (chosen.isEmpty()) {
            return;
        }
        if (!io.confirm("Remove \"" + chosen.get().title() + "\"?")) {
            return;
        }
        addressService.removeAddress(user.id(), chosen.get().id());
        io.printSuccess("Address removed.");
    }

    private void markDefaultAddress() {
        User user = session.requireCurrentUser();
        Optional<Address> chosen = chooseAddress("Which address should be the default");
        if (chosen.isEmpty()) {
            return;
        }
        addressService.markDefault(user.id(), chosen.get().id());
        io.printSuccess("\"" + chosen.get().title() + "\" is now your default address.");
    }

    private Optional<Address> chooseAddress(String prompt) {
        List<Address> addresses = addressService.listAddresses(
                session.requireCurrentUser().id());
        if (addresses.isEmpty()) {
            io.print("You have no addresses yet.");
            return Optional.empty();
        }
        return io.chooseFrom(prompt, addresses, addressLabels());
    }

    private List<String> addressLabels() {
        return addressService.listAddresses(session.requireCurrentUser().id()).stream()
                .map(address -> address + (address.isDefault() ? "  [default]" : ""))
                .toList();
    }
}
