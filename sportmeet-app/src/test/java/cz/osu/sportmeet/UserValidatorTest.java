package cz.osu.sportmeet;

import cz.osu.sportmeet.account.application.dto.CreateUserInput;
import cz.osu.sportmeet.account.application.validation.UserValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests standalone business validation without loading a Spring application context.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
class UserValidatorTest
{
    private final UserValidator validator = new UserValidator();

    /**
     * Verifies canonical case-insensitive identity normalization.
     */
    @Test
    void shouldNormalizeLoginAndEmail()
    {
        CreateUserInput normalized = validator.validateAndNormalize(
                new CreateUserInput("  RunnerOne ", " Runner One ", " Runner@Example.COM ", "safe-password-123"));

        assertThat(normalized.login()).isEqualTo("runnerone");
        assertThat(normalized.displayName()).isEqualTo("Runner One");
        assertThat(normalized.email()).isEqualTo("runner@example.com");
    }

    /**
     * Verifies that BCrypt's 72-byte input limit is applied to UTF-8 input.
     */
    @Test
    void shouldRejectPasswordLongerThan72Bytes()
    {
        assertThatThrownBy(() -> validator.validateAndNormalize(
                new CreateUserInput("runner", "Runner", "runner@example.com", "ž".repeat(37))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Rejects missing identity values even when invoked outside HTTP.
     */
    @Test
    void shouldRejectMissingLogin()
    {
        assertThatThrownBy(() -> validator.validateAndNormalize(
                new CreateUserInput(null, "Runner", "runner@example.com", "safe-password-123")))
                .isInstanceOf(NullPointerException.class);
    }

    /**
     * Verifies that the display name is required.
     */
    @Test
    void shouldRejectBlankDisplayName()
    {
        assertThatThrownBy(() -> validator.validateAndNormalize(
                new CreateUserInput("runner", "  ", "runner@example.com", "safe-password-123")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Verifies the lower and upper boundaries of the allowed login length (2 to 80 characters).
     */
    @Test
    void shouldAcceptLoginAtLengthBoundaries()
    {
        assertThat(validator.validateAndNormalize(
                new CreateUserInput("aa", "Runner", "runner@example.com", "safe-password-123")).login())
                .hasSize(2);

        assertThat(validator.validateAndNormalize(
                new CreateUserInput("a".repeat(80), "Runner", "runner@example.com", "safe-password-123")).login())
                .hasSize(80);
    }

    /**
     * Verifies that logins outside the allowed length are rejected just past each boundary.
     */
    @Test
    void shouldRejectLoginOutsideLengthBoundaries()
    {
        assertThatThrownBy(() -> validator.validateAndNormalize(
                new CreateUserInput("a", "Runner", "runner@example.com", "safe-password-123")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> validator.validateAndNormalize(
                new CreateUserInput("a".repeat(81), "Runner", "runner@example.com", "safe-password-123")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Verifies the upper boundary of the allowed display name length (255 characters).
     */
    @Test
    void shouldAcceptDisplayNameAtLengthBoundary()
    {
        assertThat(validator.validateAndNormalize(
                new CreateUserInput("runner", "a".repeat(255), "runner@example.com", "safe-password-123"))
                .displayName()).hasSize(255);
    }

    /**
     * Verifies that a display name longer than 255 characters is rejected.
     */
    @Test
    void shouldRejectDisplayNameLongerThanLengthBoundary()
    {
        assertThatThrownBy(() -> validator.validateAndNormalize(
                new CreateUserInput("runner", "a".repeat(256), "runner@example.com", "safe-password-123")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Verifies the upper boundary of the allowed email length (254 characters).
     */
    @Test
    void shouldAcceptEmailAtLengthBoundary()
    {
        String domain = "@example.com";
        String email = "a".repeat(254 - domain.length()) + domain;
        assertThat(email).hasSize(254);

        assertThat(validator.validateAndNormalize(
                new CreateUserInput("runner", "Runner", email, "safe-password-123")).email())
                .hasSize(254);
    }

    /**
     * Verifies that an email longer than 254 characters is rejected.
     */
    @Test
    void shouldRejectEmailLongerThanLengthBoundary()
    {
        String domain = "@example.com";
        String email = "a".repeat(255 - domain.length()) + domain;
        assertThat(email).hasSize(255);

        assertThatThrownBy(() -> validator.validateAndNormalize(
                new CreateUserInput("runner", "Runner", email, "safe-password-123")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Verifies an email lacking the required "@" and domain separator is rejected.
     */
    @Test
    void shouldRejectMalformedEmail()
    {
        assertThatThrownBy(() -> validator.validateAndNormalize(
                new CreateUserInput("runner", "Runner", "not-an-email", "safe-password-123")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Verifies the lower boundary of the allowed password length (8 characters).
     */
    @Test
    void shouldAcceptPasswordAtMinimumLength()
    {
        assertThat(validator.validateAndNormalize(
                new CreateUserInput("runner", "Runner", "runner@example.com", "a".repeat(8))).password())
                .hasSize(8);
    }

    /**
     * Verifies that a password shorter than 8 characters is rejected.
     */
    @Test
    void shouldRejectPasswordShorterThanMinimumLength()
    {
        assertThatThrownBy(() -> validator.validateAndNormalize(
                new CreateUserInput("runner", "Runner", "runner@example.com", "a".repeat(7))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Verifies that a password of exactly 72 UTF-8 bytes using multi-byte characters is accepted.
     */
    @Test
    void shouldAcceptPasswordAtExactly72Bytes()
    {
        String password = "ž".repeat(36);
        assertThat(password.getBytes(java.nio.charset.StandardCharsets.UTF_8)).hasSize(72);

        assertThat(validator.validateAndNormalize(
                new CreateUserInput("runner", "Runner", "runner@example.com", password)).password())
                .isEqualTo(password);
    }

}
