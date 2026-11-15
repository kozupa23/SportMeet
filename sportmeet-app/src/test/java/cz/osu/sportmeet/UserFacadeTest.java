package cz.osu.sportmeet;

import cz.osu.sportmeet.account.application.UserFacadeImpl;
import cz.osu.sportmeet.account.application.dto.CreateUserInput;
import cz.osu.sportmeet.account.application.dto.CreatedUser;
import cz.osu.sportmeet.account.application.exception.DuplicateEmailException;
import cz.osu.sportmeet.account.application.exception.DuplicateLoginException;
import cz.osu.sportmeet.account.application.validation.UserValidator;
import cz.osu.sportmeet.account.domain.UserState;
import cz.osu.sportmeet.account.persistence.entity.User;
import cz.osu.sportmeet.account.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Verifies business uniqueness checks independently of the web and database layers.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
class UserFacadeTest
{
    private final UserRepository repository = mock(UserRepository.class);
    private final UserFacadeImpl facade = new UserFacadeImpl(repository, new UserValidator(),
            new BCryptPasswordEncoder());

    /**
     * Rejects an existing login before attempting to persist an account.
     */
    @Test
    void shouldRejectExistingLogin()
    {
        when(repository.existsByLogin("running")).thenReturn(true);

        assertThatThrownBy(() -> facade.createUser(new CreateUserInput(
                "Running", "Running", "running@example.com", "safe-password-123")))
                .isInstanceOf(DuplicateLoginException.class);
    }

    /**
     * Rejects a duplicate email even when the requested login is available.
     */
    @Test
    void shouldRejectExistingEmail()
    {
        when(repository.existsByEmail("running@example.com")).thenReturn(true);

        assertThatThrownBy(() -> facade.createUser(new CreateUserInput(
                "running", "Runner", "RUNNING@EXAMPLE.COM", "safe-password-123")))
                .isInstanceOf(DuplicateEmailException.class);
    }

    /**
     * Rejects invalid identity values before querying the repository.
     */
    @Test
    void shouldRejectInvalidValuesBeforeDataAccess()
    {
        assertThatThrownBy(() -> facade.createUser(new CreateUserInput(
                "x", "X", "not-an-email", "short")))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }

    /**
     * Verifies the full success path in isolation: normalization, password encoding,
     * the persisted entity passed to the repository, and the resulting mapping.
     */
    @Test
    void shouldCreateUserSuccessfully()
    {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserFacadeImpl facade = new UserFacadeImpl(repository, new UserValidator(), passwordEncoder);

        when(repository.existsByLogin("runner")).thenReturn(false);
        when(repository.existsByEmail("runner@example.com")).thenReturn(false);
        when(passwordEncoder.encode("safe-password-123")).thenReturn("encoded-hash");

        User savedUser = mock(User.class);
        when(savedUser.getId()).thenReturn(42L);
        when(savedUser.getLogin()).thenReturn("runner");
        when(savedUser.getDisplayName()).thenReturn("Runner");
        when(savedUser.getEmail()).thenReturn("runner@example.com");
        when(savedUser.getState()).thenReturn(UserState.ACTIVE);
        when(repository.saveAndFlush(any(User.class))).thenReturn(savedUser);

        CreatedUser result = facade.createUser(new CreateUserInput(
                " Runner ", " Runner ", " Runner@Example.COM ", "safe-password-123"));

        assertThat(result).isEqualTo(new CreatedUser(42L, "runner", "Runner", "runner@example.com", UserState.ACTIVE));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getLogin()).isEqualTo("runner");
        assertThat(captor.getValue().getEmail()).isEqualTo("runner@example.com");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded-hash");
    }
}