package ir.aut.shop.model;

/**
 * One delivery address in a customer's address book (user story 7).
 *
 * <p>This is an entity rather than a value object: the customer edits, deletes and marks
 * one of their addresses as the default, so the program must be able to point at a
 * particular address even after its text changes.
 *
 * <p>An address copied onto an order is a different matter: see {@link Order}, which keeps
 * a frozen copy so that later edits to the address book cannot rewrite past orders.
 */
public class Address {

    private final AddressId id;
    private String recipientName;
    private PhoneNumber phoneNumber;
    private String country;
    private String city;
    private String line;
    private PostalCode postalCode;
    private String title;
    private boolean defaultAddress;

    /**
     * REQUIRES: every argument is non-null; the text arguments are non-blank.
     * EFFECTS: Builds a non-default address, or throws IllegalArgumentException when an
     * argument is missing.
     *
     * @param id the identity of this address
     * @param recipientName who receives the parcel
     * @param phoneNumber the recipient's contact number
     * @param country the destination country
     * @param city the destination city
     * @param line street, alley and building number
     * @param postalCode the postal code of the destination
     * @param title a short label such as "home" or "office"
     */
    public Address(AddressId id, String recipientName, PhoneNumber phoneNumber, String country,
            String city, String line, PostalCode postalCode, String title) {
        this.id = requireNonNull(id, "id");
        this.recipientName = requireText(recipientName, "recipientName");
        this.phoneNumber = requireNonNull(phoneNumber, "phoneNumber");
        this.country = requireText(country, "country");
        this.city = requireText(city, "city");
        this.line = requireText(line, "line");
        this.postalCode = requireNonNull(postalCode, "postalCode");
        this.title = requireText(title, "title");
        this.defaultAddress = false;
        checkRep();
    }

    public AddressId id() {
        return id;
    }

    public String recipientName() {
        return recipientName;
    }

    public PhoneNumber phoneNumber() {
        return phoneNumber;
    }

    public String country() {
        return country;
    }

    public String city() {
        return city;
    }

    public String line() {
        return line;
    }

    public PostalCode postalCode() {
        return postalCode;
    }

    public String title() {
        return title;
    }

    /**
     * Reports whether this is the address used unless the customer picks another.
     *
     * @return true when this address is the default one
     */
    public boolean isDefault() {
        return defaultAddress;
    }

    /**
     * REQUIRES: every argument is non-null; the text arguments are non-blank.
     * MODIFIES: this address.
     * EFFECTS: Replaces the editable details. The identity and the default flag are
     * unchanged.
     *
     * @param recipientName who receives the parcel
     * @param phoneNumber the recipient's contact number
     * @param country the destination country
     * @param city the destination city
     * @param line street, alley and building number
     * @param postalCode the postal code of the destination
     * @param title a short label such as "home" or "office"
     */
    public void updateDetails(String recipientName, PhoneNumber phoneNumber, String country,
            String city, String line, PostalCode postalCode, String title) {
        this.recipientName = requireText(recipientName, "recipientName");
        this.phoneNumber = requireNonNull(phoneNumber, "phoneNumber");
        this.country = requireText(country, "country");
        this.city = requireText(city, "city");
        this.line = requireText(line, "line");
        this.postalCode = requireNonNull(postalCode, "postalCode");
        this.title = requireText(title, "title");
        checkRep();
    }

    /**
     * MODIFIES: this address.
     * EFFECTS: Makes this the default address. Keeping only one address default is
     * {@link User}'s job, since it owns the whole address book.
     */
    void markDefault() {
        this.defaultAddress = true;
    }

    /**
     * MODIFIES: this address.
     * EFFECTS: Stops this being the default address.
     */
    void unmarkDefault() {
        this.defaultAddress = false;
    }

    /**
     * Returns an unchanging copy of this address, safe to store on an order.
     *
     * @return a copy that no later edit of this address can affect
     */
    Address frozenCopy() {
        Address copy = new Address(id, recipientName, phoneNumber, country, city, line,
                postalCode, title);
        if (defaultAddress) {
            copy.markDefault();
        }
        return copy;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Address " + field + " must not be blank");
        }
        return value.trim();
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException("Address " + field + " must not be null");
        }
        return value;
    }

    // RI: id, phoneNumber and postalCode are non-null; every text field is non-blank.
    private void checkRep() {
        assert id != null : "address id is null";
        assert phoneNumber != null : "address phone number is null";
        assert postalCode != null : "address postal code is null";
        assert recipientName != null && !recipientName.isBlank() : "recipient name is blank";
        assert country != null && !country.isBlank() : "country is blank";
        assert city != null && !city.isBlank() : "city is blank";
        assert line != null && !line.isBlank() : "address line is blank";
        assert title != null && !title.isBlank() : "address title is blank";
    }

    @Override
    public String toString() {
        return title + ": " + country + ", " + city + ", " + line + " (" + postalCode + ")";
    }
}
