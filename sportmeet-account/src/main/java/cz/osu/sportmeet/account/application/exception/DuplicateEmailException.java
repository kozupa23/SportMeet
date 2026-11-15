package cz.osu.sportmeet.account.application.exception;

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
}