package cz.osu.sportmeet.config;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.user.api.authentication.UserAuthenticationFacade;
import cz.osu.sportmeet.user.api.authentication.UserCredentials;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Adapts the user module's authentication contract to Spring Security.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Service
public final class SportMeetUserDetailsService implements UserDetailsService
{
    private final UserAuthenticationFacade userAuthenticationFacade;

    /**
     * Creates the Spring Security adapter.
     *
     * @param userAuthenticationFacade lookup contract owned by the user module
     */
    public SportMeetUserDetailsService(UserAuthenticationFacade userAuthenticationFacade)
    {
        this.userAuthenticationFacade = Preconditions.checkNotNull(
                userAuthenticationFacade, "userAuthenticationFacade must not be null");
    }

    /**
     * Finds the user and maps its status to Spring Security authentication rules.
     *
     * @param username submitted login; normalization is owned by the user module
     * @return Spring Security principal with password hash and activation state
     * @throws UsernameNotFoundException when the user does not exist
     */

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException
    {
        Preconditions.checkNotNull(username, "username must not be null");

        UserCredentials credentials = userAuthenticationFacade.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return User.withUsername(credentials.login())
                .password(credentials.passwordHash())
                .roles("USER")
                .disabled(!credentials.enabled())
                .build();
    }
}
