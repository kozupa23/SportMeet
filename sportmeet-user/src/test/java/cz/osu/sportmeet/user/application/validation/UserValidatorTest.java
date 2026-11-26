package cz.osu.sportmeet.user.application.validation;

import cz.osu.sportmeet.common.application.InvalidInputException;
import cz.osu.sportmeet.user.application.dto.CreateUserInput;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests business validation and normalization without a Spring context.
 * Rules of single values (login, display name, e-mail) are tested once on the dedicated
 * {@code normalize*} methods; registration tests cover only what registration adds on top
 * (login length, password) and that it applies the single-value rules.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
class UserValidatorTest
{
    private static final String PASSWORD = "safe-password-123";
    private static final String DOMAIN = "@example.com";

    private final UserValidator validator = new UserValidator();

    @Test
    void normalizesLoginWithoutValidatingLength()
    {
        assertThat(validator.normalizeLogin("  RunnerOne ")).isEqualTo("runnerone");
        assertThat(validator.normalizeLogin("X")).isEqualTo("x");
    }

    @Test
    void stripsDisplayNameButKeepsLetterCase()
    {
        assertThat(validator.normalizeDisplayName("  Fast Runner ")).isEqualTo("Fast Runner");
        assertThat(validator.normalizeDisplayName("a".repeat(255))).hasSize(255);
    }

    @Test
    void rejectsBlankOrTooLongDisplayName()
    {
        assertThatThrownBy(() -> validator.normalizeDisplayName("   "))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> validator.normalizeDisplayName("a".repeat(256)))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void normalizesEmailToLowercase()
    {
        assertThat(validator.normalizeEmail(" Runner@Example.COM ")).isEqualTo("runner@example.com");
        assertThat(validator.normalizeEmail("a".repeat(254 - DOMAIN.length()) + DOMAIN)).hasSize(254);
    }

    @Test
    void rejectsMalformedOrTooLongEmail()
    {
        assertThatThrownBy(() -> validator.normalizeEmail("not-an-email"))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> validator.normalizeEmail("a b" + DOMAIN))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> validator.normalizeEmail("a".repeat(255 - DOMAIN.length()) + DOMAIN))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void validationMessagesDoNotEchoSubmittedValues()
    {
        assertThatThrownBy(() -> validator.normalizeEmail("secret-value"))
                .hasMessageNotContaining("secret-value");
    }

    @Test
    void registrationNormalizesAllIdentityValues()
    {
        CreateUserInput normalized = validator.validateAndNormalize(
                new CreateUserInput("  RunnerOne ", " Runner One ", " Runner@Example.COM ", PASSWORD));

        assertThat(normalized).isEqualTo(
                new CreateUserInput("runnerone", "Runner One", "runner@example.com", PASSWORD));
    }

    @Test
    void registrationAppliesDisplayNameAndEmailRules()
    {
        assertThatThrownBy(() -> validator.validateAndNormalize(
                new CreateUserInput("runner", "  ", "runner@example.com", PASSWORD)))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> validator.validateAndNormalize(
                new CreateUserInput("runner", "Runner", "not-an-email", PASSWORD)))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void registrationAcceptsLoginBetween2And80Characters()
    {
        assertThat(register("aa", PASSWORD).login()).hasSize(2);
        assertThat(register("a".repeat(80), PASSWORD).login()).hasSize(80);
        assertThatThrownBy(() -> register("a", PASSWORD)).isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> register("a".repeat(81), PASSWORD)).isInstanceOf(InvalidInputException.class);
    }

    /**
     * BCrypt uses only the first 72 bytes, so the upper limit is in UTF-8 bytes, not characters.
     */
    @Test
    void registrationAcceptsPasswordFrom8CharactersTo72Bytes()
    {
        String password72Bytes = "ž".repeat(36);
        assertThat(password72Bytes.getBytes(StandardCharsets.UTF_8)).hasSize(72);

        assertThat(register("runner", "a".repeat(8)).password()).hasSize(8);
        assertThat(register("runner", password72Bytes).password()).isEqualTo(password72Bytes);
        assertThatThrownBy(() -> register("runner", "a".repeat(7))).isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> register("runner", "ž".repeat(37))).isInstanceOf(InvalidInputException.class);
    }

    private CreateUserInput register(String login, String password)
    {
        return validator.validateAndNormalize(new CreateUserInput(login, "Runner", "runner@example.com", password));
    }
}
