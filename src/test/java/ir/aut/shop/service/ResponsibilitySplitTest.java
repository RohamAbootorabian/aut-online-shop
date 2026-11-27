package ir.aut.shop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ir.aut.shop.model.Email;
import ir.aut.shop.model.PhoneNumber;
import ir.aut.shop.model.User;
import ir.aut.shop.repository.memory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * That the two responsibilities taken out of other classes still work on their own.
 *
 * <p>Each of these tests builds one small object and nothing else. That is the gain: a
 * reset token can be judged without an order service in the room, and the rule about who
 * may act can be judged without a product in the room.
 */
class ResponsibilitySplitTest {

    private UserService users;
    private PasswordResetService passwordReset;
    private AccessPolicy access;

    @BeforeEach
    void setUp() {
        users = new UserService(new InMemoryUserRepository());
        passwordReset = new PasswordResetService(users);
        access = new AccessPolicy();
    }

    private User customer() {
        return users.signUp(new Email("sara@shop.ir"), new PhoneNumber("09121112233"),
                "Sara", "secret123");
    }

    @Test
    @DisplayName("Story 4 and 5: a reset token works once and sets the new password")
    void resetTokenWorksOnce() {
        User user = customer();

        String token = passwordReset.requestReset(user.email());
        passwordReset.completeReset(token, "brandnew1");

        assertEquals(user.id(), users.logIn("sara@shop.ir", "brandnew1").id());
        assertThrows(IllegalArgumentException.class,
                () -> users.logIn("sara@shop.ir", "secret123"));
    }

    @Test
    @DisplayName("Story 5: a spent token cannot be used again")
    void spentTokenIsRefused() {
        User user = customer();
        String token = passwordReset.requestReset(user.email());
        passwordReset.completeReset(token, "brandnew1");

        assertThrows(IllegalArgumentException.class,
                () -> passwordReset.completeReset(token, "another1"));
    }

    @Test
    @DisplayName("Story 4: recovery is refused for an address nobody registered")
    void unknownAddressIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> passwordReset.requestReset(new Email("nobody@shop.ir")));
    }

    @Test
    @DisplayName("The rule about who may act lives in one place and reads the same everywhere")
    void accessPolicyRefusesCustomersAndStrangers() {
        User user = customer();

        assertFalse(access.isAdmin(user));
        assertFalse(access.isAdmin(null));
        assertThrows(IllegalArgumentException.class,
                () -> access.requireAdmin(user, "manage stock"));
        assertThrows(IllegalArgumentException.class,
                () -> access.requireAdmin(null, "manage stock"));
    }

    @Test
    @DisplayName("An admin passes the same rule")
    void accessPolicyAdmitsAnAdmin() {
        User admin = users.registerAdmin(new Email("admin@shop.ir"),
                new PhoneNumber("09120000000"), "Shop Manager", "admin123");

        assertTrue(access.isAdmin(admin));
        access.requireAdmin(admin, "manage stock");
    }

    @Test
    @DisplayName("The refusal names the action, so three services no longer need three messages")
    void refusalNamesTheAction() {
        User user = customer();

        IllegalArgumentException refused = assertThrows(IllegalArgumentException.class,
                () -> access.requireAdmin(user, "see every order"));

        assertEquals("Only an admin may see every order", refused.getMessage());
    }
}
