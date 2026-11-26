package cz.osu.sportmeet.common.application;

import org.jspecify.annotations.NullMarked;

/**
 * Business validation failure caused by client-supplied input.
 * Unlike {@link IllegalArgumentException}, which signals a programming error,
 * this exception is safe to translate into an HTTP 400 response; its message
 * must therefore never contain submitted values.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public class InvalidInputException extends RuntimeException
{
    /**
     * Creates a client-safe validation error.
     *
     * @param message description of the violated validation rule
     */
    public InvalidInputException(String message)
    {
        super(message);
    }

    /**
     * Throws an {@link InvalidInputException} when the condition does not hold.
     *
     * @param condition validation rule that must be satisfied
     * @param message   client-safe description of the violated rule
     * @throws InvalidInputException if the condition is false
     */
    public static void check(boolean condition, String message)
    {
        if (!condition)
        {
            throw new InvalidInputException(message);
        }
    }
}
