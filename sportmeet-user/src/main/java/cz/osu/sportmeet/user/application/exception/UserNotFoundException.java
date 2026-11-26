package cz.osu.sportmeet.user.application.exception;

import org.jspecify.annotations.NullMarked;

/**
 * The authenticated principal has no corresponding accessible user.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public final class UserNotFoundException extends RuntimeException
{
    /**
     * Creates an error indicating the user does not exist or is inactive.
     */
    public UserNotFoundException()
    {
        super("User not found.");
    }
}
