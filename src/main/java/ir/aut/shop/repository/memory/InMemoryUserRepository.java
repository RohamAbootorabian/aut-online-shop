package ir.aut.shop.repository.memory;

import ir.aut.shop.model.UserId;
import ir.aut.shop.model.User;
import ir.aut.shop.repository.UserRepository;

/**
 * Accounts kept in memory.
 */
public class InMemoryUserRepository extends InMemoryRepository<UserId, User> implements UserRepository {
}
