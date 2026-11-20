package cz.osu.sportmeet.user.persistence.repository;

import cz.osu.sportmeet.user.persistence.entity.User;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Provides persistence operations for registered users.
 * User persistence is owned exclusively by the user module.
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
     * @return true if an user with the login exists
     */
    boolean existsByLogin(String login);

    /**
     * Checks whether a canonical email is already registered.
     *
     * @param email normalized email address
     * @return true if an user with the email exists
     */
    boolean existsByEmail(String email);

    /**
     * Looks up a registered user using its canonical login.
     *
     * @param login normalized login
     * @return matching user if one exists
     */
    Optional<User> findByLogin(String login);
}