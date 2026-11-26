package cz.osu.sportmeet.user.application;

import cz.osu.sportmeet.user.api.lookup.UserSummary;
import cz.osu.sportmeet.user.application.validation.UserValidator;
import cz.osu.sportmeet.user.domain.UserState;
import cz.osu.sportmeet.user.persistence.entity.User;
import cz.osu.sportmeet.user.persistence.repository.UserRepository;
import cz.osu.sportmeet.user.support.TestUsers;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the user identifier lookup offered to other modules.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
class UserLookupFacadeImplTest
{
    private final UserRepository users = mock(UserRepository.class);
    private final UserLookupFacadeImpl facade = new UserLookupFacadeImpl(users, new UserValidator());

    @Test
    void returnsIdOfActiveUserForCanonicalLogin()
    {
        when(users.findByLogin("runner")).thenReturn(Optional.of(
                TestUsers.persisted(7L, "runner", "Runner", "runner@example.com")));

        assertThat(facade.findActiveUserId("  RUNNER ")).contains(7L);
    }

    @Test
    void returnsIdOnlyForActiveAndValidUsers()
    {
        for (UserState state : UserState.values())
        {
            for (boolean valid : new boolean[]{true, false})
            {
                User user = TestUsers.withState(
                        TestUsers.persisted(7L, "runner", "Runner", "runner@example.com"), state);
                if (!valid)
                {
                    TestUsers.invalid(user);
                }
                when(users.findByLogin("runner")).thenReturn(Optional.of(user));

                boolean expectedPresent = state == UserState.ACTIVE && valid;

                assertThat(facade.findActiveUserId("runner").isPresent())
                        .as("state=%s, valid=%s", state, valid)
                        .isEqualTo(expectedPresent);
            }
        }
    }

    @Test
    void returnsEmptyForUnknownLogin()
    {
        when(users.findByLogin("missing")).thenReturn(Optional.empty());

        assertThat(facade.findActiveUserId("missing")).isEmpty();
    }

    @Test
    void returnsPublicSummariesOfExistingUsersRegardlessOfState()
    {
        User blocked = TestUsers.withState(
                TestUsers.persisted(8L, "blocked", "Blocked Runner", "blocked@example.com"), UserState.BLOCKED);
        when(users.findAllById(List.of(7L, 8L, 99L))).thenReturn(List.of(
                TestUsers.persisted(7L, "runner", "Runner", "runner@example.com"), blocked));

        assertThat(facade.findUserSummaries(List.of(7L, 8L, 99L))).containsExactlyInAnyOrder(
                new UserSummary(7L, "Runner"),
                new UserSummary(8L, "Blocked Runner"));
    }

    @Test
    void emptyRequestDoesNotQueryDatabase()
    {
        assertThat(facade.findUserSummaries(List.of())).isEmpty();
        verifyNoInteractions(users);
    }
}
