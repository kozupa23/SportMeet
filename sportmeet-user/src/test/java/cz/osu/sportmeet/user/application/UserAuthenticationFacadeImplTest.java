package cz.osu.sportmeet.user.application;

import cz.osu.sportmeet.user.application.validation.UserValidator;
import cz.osu.sportmeet.user.domain.UserState;
import cz.osu.sportmeet.user.persistence.entity.User;
import cz.osu.sportmeet.user.persistence.repository.UserRepository;
import cz.osu.sportmeet.user.support.TestUsers;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests authentication data provided by the user module.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
class UserAuthenticationFacadeImplTest
{
    @Test
    void looksUpCanonicalLogin()
    {
        UserRepository users = mock(UserRepository.class);
        when(users.findByLogin("runner")).thenReturn(Optional.of(
                TestUsers.persisted(7L, "runner", "Runner", "runner@example.com")));

        UserAuthenticationFacadeImpl facade =
                new UserAuthenticationFacadeImpl(users, new UserValidator());

        assertThat(facade.findByLogin("  RUNNER ")).hasValueSatisfying(credentials ->
                assertThat(credentials.login()).isEqualTo("runner"));
    }

    @Test
    void shouldEnableOnlyActiveAndValidUsers()
    {
        for (UserState state : UserState.values())
        {
            for (boolean valid : new boolean[]{true, false})
            {
                User user = TestUsers.persisted(7L, "runner", "Runner", "runner@example.com");

                TestUsers.withState(user, state);

                if (!valid)
                {
                    TestUsers.invalid(user);
                }

                UserRepository users = mock(UserRepository.class);
                when(users.findByLogin("runner")).thenReturn(Optional.of(user));

                UserAuthenticationFacadeImpl facade = new UserAuthenticationFacadeImpl(
                        users, new UserValidator());

                boolean expectedEnabled = state == UserState.ACTIVE && valid;

                assertThat(facade.findByLogin("runner"))
                        .hasValueSatisfying(credentials ->
                                assertThat(credentials.enabled())
                                        .as("state=%s, valid=%s", state, valid)
                                        .isEqualTo(expectedEnabled));
            }
        }
    }

    @Test
    void shouldReturnEmptyForUnknownLogin()
    {
        UserRepository users = mock(UserRepository.class);
        when(users.findByLogin("missing"))
                .thenReturn(Optional.empty());

        UserAuthenticationFacadeImpl facade =
                new UserAuthenticationFacadeImpl(users, new UserValidator());

        assertThat(facade.findByLogin("missing")).isEmpty();
    }
}
