package cz.osu.sportmeet.user.application.validation;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import cz.osu.sportmeet.common.application.InvalidInputException;
import cz.osu.sportmeet.user.application.dto.CreateUserInput;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validates and normalizes user data.
 * Applies the same business rules independently of the
 * API or integration through which an user is created or changed.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Component
public final class UserValidator
{
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /**
     * Validates registration data and normalizes identity values.
     * Login and email are converted to lowercase.
     *
     * @param input user data requiring validation
     * @return validated user data with canonical identity values
     * @throws InvalidInputException if registration data is invalid
     */
    public CreateUserInput validateAndNormalize(CreateUserInput input)
    {
        Preconditions.checkNotNull(input, "input must not be null");

        String login = normalizeLogin(input.login());
        String displayName = normalizeDisplayName(input.displayName());
        String email = normalizeEmail(input.email());
        String password = input.password();

        InvalidInputException.check(
                login.length() >= 2 && login.length() <= 80,
                "Login must have between 2 and 80 characters.");

        InvalidInputException.check(
                password.length() >= 8 &&
                        password.getBytes(StandardCharsets.UTF_8).length <= 72,
                "Password must have at least 8 characters and at most 72 UTF-8 bytes.");

        return new CreateUserInput(login, displayName, email, password);
    }

    /**
     * Converts a login into its canonical form (stripped, lowercase).
     * Does not validate the length, so it can also be used for lookups.
     *
     * @param login submitted login
     * @return canonical login
     */
    public String normalizeLogin(String login)
    {
        Preconditions.checkNotNull(login, "login must not be null");

        return login.strip().toLowerCase(Locale.ROOT);
    }

    /**
     * Strips and validates a display name.
     *
     * @param displayName submitted visible name
     * @return stripped visible name
     * @throws InvalidInputException if the name is blank or longer than 255 characters
     */
    public String normalizeDisplayName(String displayName)
    {
        Preconditions.checkNotNull(displayName, "displayName must not be null");

        String normalized = displayName.strip();
        InvalidInputException.check(
                !Strings.isNullOrEmpty(normalized) && normalized.length() <= 255,
                "Display name must have between 1 and 255 characters.");

        return normalized;
    }

    /**
     * Strips, lowercases and validates an e-mail address.
     *
     * @param email submitted e-mail address
     * @return canonical e-mail address
     * @throws InvalidInputException if the address is invalid
     */
    public String normalizeEmail(String email)
    {
        Preconditions.checkNotNull(email, "email must not be null");

        String normalized = email.strip().toLowerCase(Locale.ROOT);
        InvalidInputException.check(
                normalized.length() <= 254 && EMAIL_PATTERN.matcher(normalized).matches(),
                "Email is invalid.");

        return normalized;
    }
}
