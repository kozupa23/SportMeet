package cz.osu.sportmeet.account.application;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.account.application.dto.AccountCredentials;
import cz.osu.sportmeet.account.persistence.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Reads authentication details from the account module's persistence layer.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Service
public class AccountAuthenticationFacadeImpl implements AccountAuthenticationFacade
{
    private final UserRepository userRepository;

    /**
     * Creates an authentication lookup facade.
     *
     * @param userRepository account-owned repository
     */
    public AccountAuthenticationFacadeImpl(UserRepository userRepository)
    {
        this.userRepository = Preconditions.checkNotNull(userRepository, "userRepository must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AccountCredentials> findByLogin(String login)
    {
        Preconditions.checkNotNull(login, "login must not be null");

        return userRepository.findByLogin(login)
                .map(user -> new AccountCredentials(
                        user.getLogin(),
                        user.getPasswordHash(),
                        user.getState(),
                        user.isValid()));
    }
}
