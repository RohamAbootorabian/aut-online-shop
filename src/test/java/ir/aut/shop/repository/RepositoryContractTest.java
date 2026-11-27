package ir.aut.shop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ir.aut.shop.model.Email;
import ir.aut.shop.model.PasswordHash;
import ir.aut.shop.model.PhoneNumber;
import ir.aut.shop.model.User;
import ir.aut.shop.model.UserId;
import ir.aut.shop.model.UserRole;
import ir.aut.shop.repository.memory.InMemoryUserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What every implementation of {@link Repository} must do.
 *
 * <p>These tests name no in-memory detail beyond the one line that builds the subject, so
 * a database-backed repository could be dropped in and judged by the same rules. That is
 * the point of writing the contract down: a replacement is substitutable only if there is
 * something for it to be measured against (Liskov).
 */
class RepositoryContractTest {

    private final UserRepository repository = new InMemoryUserRepository();

    private static User userNamed(String name) {
        return new User(UserId.newId(), new Email(name + "@shop.ir"),
                new PhoneNumber("0912000" + name.length() + "000"), name,
                PasswordHash.of("secret123"), UserRole.CUSTOMER, LocalDateTime.now());
    }

    @Test
    @DisplayName("What was saved comes back under the same identity")
    void savedItemIsFoundAgain() {
        User user = userNamed("Sara");
        repository.save(user.id(), user);

        assertEquals(user, repository.findById(user.id()).orElseThrow());
        assertTrue(repository.contains(user.id()));
    }

    @Test
    @DisplayName("An unknown identity gives an empty answer, not an error")
    void unknownIdentityIsEmpty() {
        assertTrue(repository.findById(UserId.newId()).isEmpty());
        assertFalse(repository.contains(UserId.newId()));
    }

    @Test
    @DisplayName("A null identity is a question, not a mistake")
    void nullIdentityIsEmpty() {
        assertTrue(repository.findById(null).isEmpty());
        assertFalse(repository.contains(null));
    }

    @Test
    @DisplayName("Saving under an identity that is taken replaces what was there")
    void saveReplaces() {
        User first = userNamed("Sara");
        User second = userNamed("Reza");
        repository.save(first.id(), first);
        repository.save(first.id(), second);

        assertEquals(second, repository.findById(first.id()).orElseThrow());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    @DisplayName("Neither a null identity nor a null item may be saved")
    void nullsAreRefused() {
        User user = userNamed("Sara");
        assertThrows(IllegalArgumentException.class, () -> repository.save(null, user));
        assertThrows(IllegalArgumentException.class, () -> repository.save(user.id(), null));
    }

    @Test
    @DisplayName("The list handed out is the caller's own, so the store cannot be reached through it")
    void findAllDoesNotExposeTheRepresentation() {
        User user = userNamed("Sara");
        repository.save(user.id(), user);

        List<User> all = repository.findAll();
        assertThrows(UnsupportedOperationException.class, () -> all.add(userNamed("Reza")));
        assertEquals(1, repository.findAll().size());
    }
}
