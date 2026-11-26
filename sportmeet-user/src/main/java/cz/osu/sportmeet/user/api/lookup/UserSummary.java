package cz.osu.sportmeet.user.api.lookup;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;

/**
 * Public identification of a user for other modules, for example to list group members.
 * Deliberately contains neither the login nor the e-mail: the login is half of the credentials,
 * and listing it to other users would hand out valid login names.
 *
 * @param id          user identifier
 * @param displayName visible name
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public record UserSummary(Long id, String displayName)
{
    /**
     * Validates required values.
     *
     * @param id          user identifier
     * @param displayName visible name
     */
    public UserSummary
    {
        Preconditions.checkNotNull(id, "id must not be null");
        Preconditions.checkNotNull(displayName, "displayName must not be null");
    }
}
