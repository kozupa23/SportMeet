package cz.osu.sportmeet.user.application;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.user.api.lookup.UserLookupFacade;
import cz.osu.sportmeet.user.application.validation.UserValidator;
import cz.osu.sportmeet.user.domain.UserState;
import cz.osu.sportmeet.user.persistence.entity.User;
import cz.osu.sportmeet.user.persistence.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Resolves user identifiers for other modules from the user module's persistence layer.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@Service
public class UserLookupFacadeImpl implements UserLookupFacade
{
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    /**
     * Creates the lookup facade.
     *
     * @param userRepository user-module repository
     * @param userValidator  owner of the canonical login format
     */
    public UserLookupFacadeImpl(UserRepository userRepository, UserValidator userValidator)
    {
        this.userRepository = Preconditions.checkNotNull(userRepository, "userRepository must not be null");
        this.userValidator = Preconditions.checkNotNull(userValidator, "userValidator must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findActiveUserId(String login)
    {
        Preconditions.checkNotNull(login, "login must not be null");

        return userRepository.findByLogin(userValidator.normalizeLogin(login))
                .filter(user -> user.isValid() && user.getState() == UserState.ACTIVE)
                .map(User::getId);
    }
}
