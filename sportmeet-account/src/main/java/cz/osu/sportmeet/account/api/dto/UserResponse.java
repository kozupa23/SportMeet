package cz.osu.sportmeet.account.api.dto;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.account.domain.UserState;
import org.jspecify.annotations.NullMarked;


/**
 * Represents the public response returned after account registration.
 * Does not expose password hashes or internal audit information.
 *
 * @param id          identifier of the created account
 * @param login       canonical account login
 * @param displayName visible name
 * @param email       canonical account email
 * @param state       current account state
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
     * @param id          account identifier
     * @param login       canonical login
     * @param displayName visible name
     * @param email       canonical email
     * @param state       account state
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