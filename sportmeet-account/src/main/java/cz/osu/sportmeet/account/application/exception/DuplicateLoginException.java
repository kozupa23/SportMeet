package cz.osu.sportmeet.account.application.exception;

import org.jspecify.annotations.NullMarked;

/**
 * Indicates that the requested account login is already registered.
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
}