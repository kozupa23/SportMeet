
package cz.osu.sportmeet.user.api.authentication;

import org.jspecify.annotations.NullMarked;

import java.util.Optional;

/**
 * User-module contract for authentication.
 */
@NullMarked
public interface UserAuthenticationFacade
{
    /**
     * Finds credentials for a submitted login.
     *
     * @param login submitted login
     * @return credentials or empty if not found
     */
    Optional<UserCredentials> findByLogin(String login);
}
