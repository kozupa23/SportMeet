package cz.osu.sportmeet.user.application.dto;

import cz.osu.sportmeet.user.domain.UserState;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * Public projection of the authenticated user.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record UserDetail(Long id, String login, String displayName, String email,
                         UserState state, OffsetDateTime createdAt, @Nullable OffsetDateTime updatedAt)
{
}
