package cz.osu.sportmeet.user.api.dto;

import cz.osu.sportmeet.user.domain.UserState;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * REST representation of the authenticated user.
 * Does not expose the password hash or internal audit actors.
 *
 * @param id          user identifier
 * @param login       canonical login
 * @param displayName visible name
 * @param email       canonical e-mail address
 * @param state       user state
 * @param createdAt   creation time
 * @param updatedAt   time of the last modification, or null if never modified
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record UserDetailResponse(Long id, String login, String displayName, String email,
                                 UserState state, OffsetDateTime createdAt, @Nullable OffsetDateTime updatedAt)
{
}
