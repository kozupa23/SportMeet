package cz.osu.sportmeet.user.application.dto;

import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.user.domain.UserHistoryAttribute;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * Application view of one user attribute change.
 *
 * @param id            history entry identifier
 * @param attributeName changed attribute
 * @param oldValue      previous value; null for secret attributes
 * @param newValue      new value; null for secret attributes
 * @param changedBy     identifier of the acting user; null for SYSTEM
 * @param changedByType category of the actor
 * @param changedAt     time of the change
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record UserHistoryEntry(Long id, UserHistoryAttribute attributeName,
                               @Nullable String oldValue, @Nullable String newValue,
                               @Nullable Long changedBy, DataInfoObjectType changedByType,
                               OffsetDateTime changedAt)
{
}
