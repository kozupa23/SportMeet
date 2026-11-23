package cz.osu.sportmeet.group.application.exception;

import org.jspecify.annotations.NullMarked;

/**
 * The signed-in user no longer has an active account (for example it was blocked after signing in)
 * and therefore may not perform group operations.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public final class InactiveUserException extends RuntimeException
{
    /**
     * Creates an error indicating that the acting user is not active.
     */
    public InactiveUserException()
    {
        super("The user account is not active.");
    }
}
