package cz.osu.sportmeet.user.application;

import cz.osu.sportmeet.common.application.InvalidInputException;
import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.user.application.dto.CreateUserInput;
import cz.osu.sportmeet.user.application.dto.CreatedUser;
import cz.osu.sportmeet.user.application.exception.DuplicateEmailException;
import cz.osu.sportmeet.user.application.exception.DuplicateLoginException;
import cz.osu.sportmeet.user.application.validation.UserValidator;
import cz.osu.sportmeet.user.domain.UserState;
import cz.osu.sportmeet.user.persistence.entity.User;
import cz.osu.sportmeet.user.persistence.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Verifies the registration use case independently of the web and database layers.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
class UserFacadeImplTest
{
    private static final String PASSWORD = "safe-password-123";

    private final UserRepository repository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserFacadeImpl facade = new UserFacadeImpl(repository, new UserValidator(), passwordEncoder);

    /**
     * Rejects an existing login, compared in canonical form, without persisting anything.
     */
    @Test
    void rejectsExistingLogin()
    {
        when(repository.existsByLogin("running")).thenReturn(true);

        assertThatThrownBy(() -> facade.createUser(
                new CreateUserInput("Running", "Running", "running@example.com", PASSWORD)))
                .isInstanceOf(DuplicateLoginException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    /**
     * Rejects a duplicate e-mail, compared in canonical form, even when the login is available.
     */
    @Test
    void rejectsExistingEmail()
    {
        when(repository.existsByEmail("running@example.com")).thenReturn(true);

        assertThatThrownBy(() -> facade.createUser(
                new CreateUserInput("running", "Runner", "RUNNING@EXAMPLE.COM", PASSWORD)))
                .isInstanceOf(DuplicateEmailException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    /**
     * Rejects invalid values before querying the repository or encoding the password.
     */
    @Test
    void rejectsInvalidValuesBeforeDataAccess()
    {
        assertThatThrownBy(() -> facade.createUser(new CreateUserInput("x", "X", "not-an-email", "short")))
                .isInstanceOf(InvalidInputException.class);
        verifyNoInteractions(repository, passwordEncoder);
    }

    @Test
    void translatesConcurrentIdentityConflictsWithoutMaskingOtherFailures()
    {
        when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded-hash");
        CreateUserInput input = new CreateUserInput("runner", "Runner", "runner@example.com", PASSWORD);
        DataIntegrityViolationException duplicateLogin = constraint("23505", "uq_t_user_login");
        DataIntegrityViolationException duplicateEmail = constraint("23505", "uq_t_user_email");
        DataIntegrityViolationException unrelated = constraint("23514", "ck_t_user_state");
        DataIntegrityViolationException unrelatedUnique = constraint("23505", "some_other_constraint");
        when(repository.saveAndFlush(any(User.class)))
                .thenThrow(duplicateLogin, duplicateEmail, unrelated, unrelatedUnique);

        assertThatThrownBy(() -> facade.createUser(input)).isInstanceOf(DuplicateLoginException.class)
                .hasCause(duplicateLogin);
        assertThatThrownBy(() -> facade.createUser(input)).isInstanceOf(DuplicateEmailException.class)
                .hasCause(duplicateEmail);
        assertThatThrownBy(() -> facade.createUser(input)).isSameAs(unrelated);
        assertThatThrownBy(() -> facade.createUser(input)).isSameAs(unrelatedUnique);
    }

    private static DataIntegrityViolationException constraint(String sqlState, String name)
    {
        return new DataIntegrityViolationException("constraint",
                new ConstraintViolationException("constraint", new SQLException("constraint", sqlState), name));
    }

    /**
     * Persists a normalized, active user with an encoded password and SYSTEM creation audit.
     */
    @Test
    void createsActiveUserWithEncodedPassword()
    {
        when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded-hash");
        when(repository.saveAndFlush(any(User.class))).thenAnswer(call ->
        {
            User user = call.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 42L);
            return user;
        });

        CreatedUser result = facade.createUser(
                new CreateUserInput(" Runner ", " Runner ", " Runner@Example.COM ", PASSWORD));

        assertThat(result).isEqualTo(new CreatedUser(42L, "runner", "Runner", "runner@example.com", UserState.ACTIVE));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("encoded-hash");
        assertThat(saved.getValue().isValid()).isTrue();
        assertThat(saved.getValue().getDataInfo().getCreatedByObjectType()).isEqualTo(DataInfoObjectType.SYSTEM);
    }
}
