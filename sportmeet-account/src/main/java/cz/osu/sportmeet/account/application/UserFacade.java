package cz.osu.sportmeet.account.application;

import cz.osu.sportmeet.account.application.dto.CreateUserInput;
import cz.osu.sportmeet.account.application.dto.CreatedUser;
import cz.osu.sportmeet.account.application.exception.DuplicateEmailException;
import cz.osu.sportmeet.account.application.exception.DuplicateLoginException;
import org.jspecify.annotations.NullMarked;

/**
 * Provides application-level operations for managing
 * user accounts.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public interface UserFacade
{
    /**
     * Creates a new user account in the ACTIVE state.
     * Normalizes identity values and verifies account uniqueness.
     *
     * @param input data required to create the account
     * @return information about the newly created account
     * @throws DuplicateLoginException  if the login already exists
     * @throws DuplicateEmailException  if the email already exists
     * @throws IllegalArgumentException if registration data is invalid
     */
    CreatedUser createUser(CreateUserInput input);
}