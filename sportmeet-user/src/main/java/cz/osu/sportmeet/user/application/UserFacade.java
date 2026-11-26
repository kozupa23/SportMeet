package cz.osu.sportmeet.user.application;

import cz.osu.sportmeet.user.application.dto.CreateUserInput;
import cz.osu.sportmeet.user.application.dto.CreatedUser;
import cz.osu.sportmeet.user.application.exception.DuplicateEmailException;
import cz.osu.sportmeet.user.application.exception.DuplicateLoginException;
import org.jspecify.annotations.NullMarked;

/**
 * Provides application-level operations for managing
 * users.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public interface UserFacade
{
    /**
     * Creates a new user in the ACTIVE state.
     * Normalizes identity values and verifies user uniqueness.
     *
     * @param input data required to create the user
     * @return information about the newly created user
     * @throws DuplicateLoginException  if the login already exists
     * @throws DuplicateEmailException  if the email already exists
     * @throws IllegalArgumentException if registration data is invalid
     */
    CreatedUser createUser(CreateUserInput input);
}