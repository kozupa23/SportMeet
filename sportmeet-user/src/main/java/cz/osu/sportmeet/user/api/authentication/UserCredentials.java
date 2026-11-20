
package cz.osu.sportmeet.user.api.authentication;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;

/**
 * Authentication data exposed by the user module.
 * Password hashes must not be logged or serialized.
 *
 * @param login canonical login
 * @param passwordHash encoded password
 * @param enabled whether authentication is allowed
 */
@NullMarked
public record UserCredentials(
        String login,
        String passwordHash,
        boolean enabled)
{
    /**
     * Validates required values.
     */
    public UserCredentials
    {
        Preconditions.checkNotNull(login);
        Preconditions.checkNotNull(passwordHash);
    }

    /**
     * Returns a representation without the password hash.
     *
     * @return redacted description
     */
    @Override
    public String toString()
    {
        return "UserCredentials[passwordHash=<redacted>, enabled=" + enabled + "]";
    }
}
