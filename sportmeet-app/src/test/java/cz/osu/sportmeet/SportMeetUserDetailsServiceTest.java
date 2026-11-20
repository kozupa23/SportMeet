package cz.osu.sportmeet;

import cz.osu.sportmeet.config.SportMeetUserDetailsService;
import cz.osu.sportmeet.user.api.authentication.UserAuthenticationFacade;
import cz.osu.sportmeet.user.api.authentication.UserCredentials;
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
 * Tests mapping of user authentication data to Spring Security.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
class SportMeetUserDetailsServiceTest
{
    @Test
    void shouldEnableUserWhenAuthenticationIsAllowed()
    {
        UserDetails details = loadUserDetails(true);

        assertThat(details.getUsername()).isEqualTo("runner");
        assertThat(details.getPassword()).isEqualTo("hashed-password");
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void shouldDisableUserWhenAuthenticationIsNotAllowed()
    {
        UserDetails details = loadUserDetails(false);

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void shouldRejectUnknownLogin()
    {
        UserAuthenticationFacade facade = mock(UserAuthenticationFacade.class);
        when(facade.findByLogin("missing")).thenReturn(Optional.empty());

        SportMeetUserDetailsService service = new SportMeetUserDetailsService(facade);

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private UserDetails loadUserDetails(boolean enabled)
    {
        UserAuthenticationFacade facade = mock(UserAuthenticationFacade.class);
        when(facade.findByLogin(" RUNNER ")).thenReturn(Optional.of(
                new UserCredentials("runner", "hashed-password", enabled)));

        return new SportMeetUserDetailsService(facade)
                .loadUserByUsername(" RUNNER ");
    }
}
