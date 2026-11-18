package cz.osu.sportmeet.config;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.account.application.AccountAuthenticationFacade;
import cz.osu.sportmeet.account.application.dto.AccountCredentials;
import cz.osu.sportmeet.account.domain.UserState;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Adapts the account module's authentication contract to Spring Security.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Service
public final class AccountUserDetailsService implements UserDetailsService
{
    private final AccountAuthenticationFacade accountAuthenticationFacade;

    /**
     * Creates the Spring Security adapter.
     *
     * @param accountAuthenticationFacade lookup contract owned by the account module
     */
    public AccountUserDetailsService(AccountAuthenticationFacade accountAuthenticationFacade)
    {
        this.accountAuthenticationFacade = Preconditions.checkNotNull(
                accountAuthenticationFacade, "accountAuthenticationFacade must not be null");
    }

    /**
     * Finds the account and maps its status to Spring Security authentication rules.
     *
     * @param username submitted account login
     * @return Spring Security principal with password hash and activation state
     * @throws UsernameNotFoundException when the account does not exist
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        Preconditions.checkNotNull(username, "username must not be null");
        String login = username.strip().toLowerCase(Locale.ROOT);

        AccountCredentials credentials = accountAuthenticationFacade.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return User.withUsername(credentials.login())
                .password(credentials.passwordHash())
                .roles("USER")
                .disabled(!credentials.valid() || credentials.state() != UserState.ACTIVE)
                .build();
    }
}
