package cz.osu.sportmeet.account.application.validation;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import cz.osu.sportmeet.account.application.dto.CreateUserInput;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validates and normalizes user registration data.
 * Applies the same business rules independently of the
 * API or integration through which an account is created.
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
     * Login and email are converted to lowercase using Locale.ROOT.
     *
     * @param input account data requiring validation
     * @return validated account data with canonical identity values
     * @throws IllegalArgumentException if registration data is invalid
     */
    public CreateUserInput validateAndNormalize(CreateUserInput input)
    {
        Preconditions.checkNotNull(input, "input must not be null");

        String login = input.login().strip().toLowerCase(Locale.ROOT);
        String email = input.email().strip().toLowerCase(Locale.ROOT);
        String displayName = input.displayName().strip();
        String password = input.password();

        Preconditions.checkArgument(
                login.length() >= 2 && login.length() <= 80,
                "Login must have between 2 and 80 characters.");

        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(displayName) &&
                        displayName.length() <= 255,
                "Display name must have between 1 and 255 characters.");

        Preconditions.checkArgument(
                email.length() <= 254 && EMAIL_PATTERN.matcher(email).matches(),
                "Email is invalid.");

        Preconditions.checkArgument(
                password.length() >= 8 &&
                        password.getBytes(StandardCharsets.UTF_8).length <= 72,
                "Password must have at least 8 characters and at most 72 UTF-8 bytes.");

        return new CreateUserInput(login, displayName, email, password);
    }
}