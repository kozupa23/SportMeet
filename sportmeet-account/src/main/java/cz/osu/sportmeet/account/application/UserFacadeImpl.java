
package cz.osu.sportmeet.account.application;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.account.application.dto.CreateUserInput;
import cz.osu.sportmeet.account.application.dto.CreatedUser;
import cz.osu.sportmeet.account.application.exception.DuplicateEmailException;
import cz.osu.sportmeet.account.application.exception.DuplicateLoginException;
import cz.osu.sportmeet.account.application.validation.UserValidator;
import cz.osu.sportmeet.account.persistence.entity.User;
import cz.osu.sportmeet.account.persistence.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Implements account management operations provided by UserFacade.
 * Coordinates validation, password encoding and persistence.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Service
public class UserFacadeImpl implements UserFacade
{
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;

    /**
     * Initializes the facade with its required dependencies.
     *
     * @param userRepository  repository for user persistence
     * @param userValidator   validator of registration data
     * @param passwordEncoder encoder for account passwords
     */
    public UserFacadeImpl(
            UserRepository userRepository,
            UserValidator userValidator,
            PasswordEncoder passwordEncoder)
    {
        this.userRepository = Preconditions.checkNotNull(
                userRepository, "userRepository must not be null");

        this.userValidator = Preconditions.checkNotNull(
                userValidator, "userValidator must not be null");

        this.passwordEncoder = Preconditions.checkNotNull(
                passwordEncoder, "passwordEncoder must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CreatedUser createUser(CreateUserInput input)
    {
        Preconditions.checkNotNull(input, "input must not be null");

        CreateUserInput createUserInput = userValidator.validateAndNormalize(input);

        if (userRepository.existsByLogin(createUserInput.login()))
        {
            throw new DuplicateLoginException();
        }

        if (userRepository.existsByEmail(createUserInput.email()))
        {
            throw new DuplicateEmailException();
        }

        User user = User.create(
                createUserInput.login(),
                createUserInput.displayName(),
                createUserInput.email(),
                passwordEncoder.encode(createUserInput.password()),
                OffsetDateTime.now(ZoneOffset.UTC));

        User savedUser = userRepository.saveAndFlush(user);

        return new CreatedUser(
                Preconditions.checkNotNull(savedUser.getId(), "Saved user ID must not be null"),
                savedUser.getLogin(),
                savedUser.getDisplayName(),
                savedUser.getEmail(),
                savedUser.getState());
    }
}
