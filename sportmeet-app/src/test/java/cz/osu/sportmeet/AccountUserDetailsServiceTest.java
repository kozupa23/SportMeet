package cz.osu.sportmeet;

import cz.osu.sportmeet.account.application.AccountAuthenticationFacade;
import cz.osu.sportmeet.account.application.dto.AccountCredentials;
import cz.osu.sportmeet.account.domain.UserState;
import cz.osu.sportmeet.config.AccountUserDetailsService;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the mapping of account lifecycle states to Spring Security principals.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
class AccountUserDetailsServiceTest
{
    @Test
    void shouldEnableOnlyActiveAndValidAccounts()
    {
        for (UserState state : UserState.values())
        {
            for (boolean valid : new boolean[]{true, false})
            {
                AccountAuthenticationFacade facade = mock(AccountAuthenticationFacade.class);
                when(facade.findByLogin("runner")).thenReturn(Optional.of(
                        new AccountCredentials("runner", "hashed-password", state, valid)));

                UserDetails details = new AccountUserDetailsService(facade).loadUserByUsername(" RUNNER ");

                assertThat(details.isEnabled()).isEqualTo(state == UserState.ACTIVE && valid);
            }
        }
    }

    @Test
    void shouldRejectUnknownLogin()
    {
        AccountAuthenticationFacade facade = mock(AccountAuthenticationFacade.class);
        when(facade.findByLogin("missing")).thenReturn(Optional.empty());

        AccountUserDetailsService service = new AccountUserDetailsService(facade);

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
