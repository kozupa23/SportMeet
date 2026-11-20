package cz.osu.sportmeet.user.application;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.user.api.authentication.UserAuthenticationFacade;
import cz.osu.sportmeet.user.api.authentication.UserCredentials;
import cz.osu.sportmeet.user.application.validation.UserValidator;
import cz.osu.sportmeet.user.domain.UserState;
import cz.osu.sportmeet.user.persistence.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Reads authentication details from the user module's persistence layer.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Service
public class UserAuthenticationFacadeImpl implements UserAuthenticationFacade
{
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    /**
     * Creates an authentication lookup facade.
     *
     * @param userRepository user-module repository
     * @param userValidator  owner of the canonical login format
     */
    public UserAuthenticationFacadeImpl(UserRepository userRepository, UserValidator userValidator)
    {
        this.userRepository = Preconditions.checkNotNull(userRepository, "userRepository must not be null");
        this.userValidator = Preconditions.checkNotNull(userValidator, "userValidator must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserCredentials> findByLogin(String login)
    {
        Preconditions.checkNotNull(login, "login must not be null");

        return userRepository.findByLogin(userValidator.normalizeLogin(login))
                .map(user -> new UserCredentials(
                        user.getLogin(),
                        user.getPasswordHash(),
                        user.isValid() && user.getState() == UserState.ACTIVE));
    }
}
