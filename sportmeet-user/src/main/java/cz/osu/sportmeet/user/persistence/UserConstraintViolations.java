package cz.osu.sportmeet.user.persistence;

import cz.osu.sportmeet.user.application.exception.DuplicateEmailException;
import cz.osu.sportmeet.user.application.exception.DuplicateLoginException;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.NullMarked;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Translates known user identity constraints without hiding other database failures.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public final class UserConstraintViolations
{
    private UserConstraintViolations()
    {
    }

    /**
     * Translates known user uniqueness constraints into application exceptions.
     * Unrecognized persistence failures are returned unchanged.
     *
     * @param exception database integrity violation
     * @return matching duplicate-identity exception, or the original exception
     */
    public static RuntimeException translate(DataIntegrityViolationException exception)
    {
        Throwable cause = exception;
        while (cause != null)
        {
            if (cause instanceof ConstraintViolationException violation
                    && "23505".equals(violation.getSQLState())
                    && violation.getConstraintName() != null)
            {
                String name = violation.getConstraintName().toUpperCase();
                if (matchesConstraint(violation, name, "UQ_T_USER_EMAIL"))
                {
                    return new DuplicateEmailException(exception);
                }
                if (matchesConstraint(violation, name, "UQ_T_USER_LOGIN"))
                {
                    return new DuplicateLoginException(exception);
                }
            }
            cause = cause.getCause();
        }

        return exception;
    }

    private static boolean matchesConstraint(ConstraintViolationException violation, String name, String expected)
    {
        if (name.equals(expected))
        {
            return true;
        }

        // H2 reports both the constraint and its generated index instead of just the constraint.
        return violation.getSQLException().getClass().getName().startsWith("org.h2.")
                && name.matches("(?:[A-Z0-9_]+\\.)?" + expected + " INDEX "
                + "(?:[A-Z0-9_]+\\.)?" + expected + "_INDEX_[0-9A-F]+");
    }
}
