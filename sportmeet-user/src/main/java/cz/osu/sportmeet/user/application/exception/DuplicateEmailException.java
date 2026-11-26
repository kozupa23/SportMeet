package cz.osu.sportmeet.user.application.exception;

import org.jspecify.annotations.NullMarked;

/**
 * Indicates that the requested email is already registered.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public final class DuplicateEmailException extends RuntimeException
{
    /**
     * Creates an exception describing the duplicate email.
     */
    public DuplicateEmailException()
    {
        super("The supplied email is already registered.");
    }

    /**
     * Creates a duplicate e-mail error preserving its database cause.
     *
     * @param cause underlying persistence error
     */
    public DuplicateEmailException(Throwable cause)
    {
        super("The supplied email is already registered.", cause);
    }
}