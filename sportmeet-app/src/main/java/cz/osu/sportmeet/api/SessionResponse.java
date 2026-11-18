package cz.osu.sportmeet.api;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;

/**
 * Minimal public information about the authenticated session.
 *
 * @param login canonical login of the signed-in account
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record SessionResponse(String login)
{
    /**
     * Validates the session response.
     *
     * @param login canonical account login
     */
    public SessionResponse
    {
        Preconditions.checkNotNull(login, "login must not be null");
    }
}
