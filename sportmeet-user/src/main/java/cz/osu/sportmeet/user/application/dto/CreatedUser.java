package cz.osu.sportmeet.user.application.dto;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.user.domain.UserState;
import org.jspecify.annotations.NullMarked;


/**
 * Provides non-sensitive information about a newly created user.
 *
 * @param id          identifier of the persisted user
 * @param login       canonical user login
 * @param displayName visible name
 * @param email       canonical email address
 * @param state       initial user state
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record CreatedUser(
        Long id,
        String login,
        String displayName,
        String email,
        UserState state)
{
    /**
     * Validates the required values of a created user.
     *
     * @param id          user identifier
     * @param login       canonical login
     * @param displayName visible name
     * @param email       canonical email
     * @param state       user state
     */
    public CreatedUser
    {
        Preconditions.checkNotNull(id, "id must not be null");
        Preconditions.checkNotNull(login, "login must not be null");
        Preconditions.checkNotNull(displayName, "displayName must not be null");
        Preconditions.checkNotNull(email, "email must not be null");
        Preconditions.checkNotNull(state, "state must not be null");
    }
}