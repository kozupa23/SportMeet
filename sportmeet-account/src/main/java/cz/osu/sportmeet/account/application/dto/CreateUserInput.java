package cz.osu.sportmeet.account.application.dto;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;

/**
 * Carries user registration data into the account application layer.
 * The values are validated and normalized before persistence.
 *
 * @param login       requested account login
 * @param displayName visible name
 * @param email       requested email address
 * @param password    plaintext password to be securely encoded
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record CreateUserInput(
        String login,
        String displayName,
        String email,
        String password)
{
    /**
     * Validates that all required registration values are present.
     *
     * @param login       requested account login
     * @param displayName visible name
     * @param email       requested email address
     * @param password    plaintext password
     */
    public CreateUserInput
    {
        Preconditions.checkNotNull(login, "login must not be null");
        Preconditions.checkNotNull(displayName, "displayName must not be null");
        Preconditions.checkNotNull(email, "email must not be null");
        Preconditions.checkNotNull(password, "password must not be null");
    }
}