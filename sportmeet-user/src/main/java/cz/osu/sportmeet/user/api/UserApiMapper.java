package cz.osu.sportmeet.user.api;

import cz.osu.sportmeet.user.api.dto.HistoryEntryResponse;
import cz.osu.sportmeet.user.api.dto.UpdateUserRequest;
import cz.osu.sportmeet.user.api.dto.UserDetailResponse;
import cz.osu.sportmeet.user.application.dto.UpdateUserInput;
import cz.osu.sportmeet.user.application.dto.UserDetail;
import cz.osu.sportmeet.user.application.dto.UserHistoryEntry;
import org.jspecify.annotations.NullMarked;

/**
 * Converts between the current-user REST contract and the application contract.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
final class UserApiMapper
{
    private UserApiMapper()
    {
    }

    static UpdateUserInput toInput(UpdateUserRequest request)
    {
        return new UpdateUserInput(request.displayName(), request.email());
    }

    static UserDetailResponse toResponse(UserDetail userDetail)
    {
        return new UserDetailResponse(userDetail.id(), userDetail.login(), userDetail.displayName(), userDetail.email(),
                userDetail.state(), userDetail.createdAt(), userDetail.updatedAt());
    }

    static HistoryEntryResponse toResponse(UserHistoryEntry entry)
    {
        return new HistoryEntryResponse(entry.id(), entry.attributeName(), entry.oldValue(), entry.newValue(),
                entry.changedBy(), entry.changedByType(), entry.changedAt());
    }
}
