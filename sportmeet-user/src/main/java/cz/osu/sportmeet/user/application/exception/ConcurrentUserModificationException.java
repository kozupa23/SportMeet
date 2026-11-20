package cz.osu.sportmeet.user.application.exception;

import org.jspecify.annotations.NullMarked;

/**
 * The user was modified concurrently by another request (optimistic lock conflict).
 * The client should reload the user and repeat the change.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public final class ConcurrentUserModificationException extends RuntimeException
{
    /**
     * Creates an error for a conflicting concurrent user modification.
     *
     * @param cause underlying optimistic locking exception
     */
    public ConcurrentUserModificationException(Throwable cause)
    {
        super("The user was modified concurrently.", cause);
    }
}
