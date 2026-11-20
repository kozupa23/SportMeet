package cz.osu.sportmeet.user.application.exception;

import org.jspecify.annotations.NullMarked;

/**
 * Indicates that the requested user login is already registered.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public final class DuplicateLoginException extends RuntimeException
{
    /**
     * Creates an exception describing the duplicate login.
     */
    public DuplicateLoginException()
    {
        super("The supplied login is already registered.");
    }

    /**
     * Creates a duplicate login error preserving its database cause.
     *
     * @param cause underlying persistence error
     */
    public DuplicateLoginException(Throwable cause)
    {
        super("The supplied login is already registered.", cause);
    }
}