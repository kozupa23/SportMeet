package cz.osu.sportmeet.account.persistence.repository;

import cz.osu.sportmeet.account.persistence.entity.User;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Provides persistence operations for registered user accounts.
 * Account persistence is owned exclusively by the account module.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public interface UserRepository extends JpaRepository<User, Long>
{
    /**
     * Checks whether a canonical login is already registered.
     *
     * @param login normalized login
     * @return true if an account with the login exists
     */
    boolean existsByLogin(String login);

    /**
     * Checks whether a canonical email is already registered.
     *
     * @param email normalized email address
     * @return true if an account with the email exists
     */
    boolean existsByEmail(String email);
}