package cz.osu.sportmeet.user.api.dto;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.user.domain.UserState;
import org.jspecify.annotations.NullMarked;


/**
 * Represents the public response returned after user registration.
 * Does not expose password hashes or internal audit information.
 *
 * @param id          identifier of the created user
 * @param login       canonical user login
 * @param displayName visible name
 * @param email       canonical user email
 * @param state       current user state
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record UserResponse(
        Long id,
        String login,
        String displayName,
        String email,
        UserState state)
{
    /**
     * Validates required response attributes.
     *
     * @param id          user identifier
     * @param login       canonical login
     * @param displayName visible name
     * @param email       canonical email
     * @param state       user state
     */
    public UserResponse
    {
        Preconditions.checkNotNull(id, "id must not be null");
        Preconditions.checkNotNull(login, "login must not be null");
        Preconditions.checkNotNull(displayName, "displayName must not be null");
        Preconditions.checkNotNull(email, "email must not be null");
        Preconditions.checkNotNull(state, "state must not be null");
    }
}