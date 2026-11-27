package ir.aut.shop.repository;

import ir.aut.shop.model.UserId;
import ir.aut.shop.model.User;

/**
 * Where accounts are kept.
 */
public interface UserRepository extends Repository<UserId, User> {
}
