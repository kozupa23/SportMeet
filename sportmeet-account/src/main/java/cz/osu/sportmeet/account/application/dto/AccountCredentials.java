package cz.osu.sportmeet.account.application.dto;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.account.domain.UserState;
import org.jspecify.annotations.NullMarked;

/**
 * Authentication data exposed by the account module to the application composition root.
 * The password hash must never be serialized or logged.
 *
 * @param login        canonical login
 * @param passwordHash encoded password
 * @param state        current account state
 * @param valid        whether the account is technically valid
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record AccountCredentials(String login, String passwordHash, UserState state, boolean valid)
{
    /**
     * Validates authentication data.
     *
     * @param login        canonical login
     * @param passwordHash encoded password
     * @param state        account state
     * @param valid        technical validity
     */
    public AccountCredentials
    {
        Preconditions.checkNotNull(login, "login must not be null");
        Preconditions.checkNotNull(passwordHash, "passwordHash must not be null");
        Preconditions.checkNotNull(state, "state must not be null");
    }

    /**
     * Produces a safe diagnostic representation without credential material.
     *
     * @return redacted diagnostic representation
     */
    @Override
    public String toString()
    {
        return "AccountCredentials[passwordHash=<redacted>, state=" + state + ", valid=" + valid + "]";
    }
}
