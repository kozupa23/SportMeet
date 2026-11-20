package cz.osu.sportmeet.user.application;

import cz.osu.sportmeet.common.application.PageResult;
import cz.osu.sportmeet.user.application.dto.UpdateUserInput;
import cz.osu.sportmeet.user.application.dto.UserDetail;
import cz.osu.sportmeet.user.application.dto.UserHistoryEntry;
import org.jspecify.annotations.NullMarked;

/**
 * Use cases for viewing and editing the authenticated user.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public interface CurrentUserFacade
{
    /**
     * Returns the authenticated user's user detail.
     *
     * @param login canonical login of the authenticated user
     * @return user detail
     * @throws cz.osu.sportmeet.user.application.exception.UserNotFoundException if the user does not exist or is not active
     */
    UserDetail getCurrentUser(String login);

    /**
     * Changes the display name and/or e-mail; {@code null} values are left unchanged.
     * Every actual change is recorded in the user history in the same transaction.
     *
     * @param login canonical login of the authenticated user
     * @param input requested changes
     * @return user detail after the change
     * @throws cz.osu.sportmeet.common.application.InvalidInputException                       if no value is provided or a value is invalid
     * @throws cz.osu.sportmeet.user.application.exception.DuplicateEmailException             if the e-mail belongs to another user
     * @throws cz.osu.sportmeet.user.application.exception.ConcurrentUserModificationException if the user was changed concurrently
     */
    UserDetail updateCurrentUser(String login, UpdateUserInput input);

    /**
     * Lists user changes newest first.
     *
     * @param login canonical login of the authenticated user
     * @param page  zero-based page index
     * @param size  page size between 1 and 100
     * @return requested page of history entries
     * @throws cz.osu.sportmeet.common.application.InvalidInputException if paging is invalid
     */
    PageResult<UserHistoryEntry> getHistory(String login, int page, int size);
}
