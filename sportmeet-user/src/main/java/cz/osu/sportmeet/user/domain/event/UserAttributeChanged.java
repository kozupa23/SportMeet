package cz.osu.sportmeet.user.domain.event;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.user.domain.UserHistoryAttribute;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * Domain event raised whenever a tracked user attribute changes.
 * Published synchronously by Spring Data when the user is saved, so listeners run
 * inside the same transaction as the change. Other modules may listen to it as well.
 *
 * @param userId        identifier of the changed user
 * @param attribute     changed attribute
 * @param oldValue      previous value; always null for secret attributes
 * @param newValue      new value; always null for secret attributes
 * @param changedBy     identifier of the acting user; null for SYSTEM
 * @param changedByType category of the actor
 * @param changedAt     time of the change
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record UserAttributeChanged(
        Long userId,
        UserHistoryAttribute attribute,
        @Nullable String oldValue,
        @Nullable String newValue,
        @Nullable Long changedBy,
        DataInfoObjectType changedByType,
        OffsetDateTime changedAt)
{
    public UserAttributeChanged
    {
        Preconditions.checkNotNull(userId, "userId must not be null");
        Preconditions.checkNotNull(attribute, "attribute must not be null");
        Preconditions.checkNotNull(changedByType, "changedByType must not be null");
        Preconditions.checkNotNull(changedAt, "changedAt must not be null");
        changedByType.validateActorId(changedBy);
        Preconditions.checkArgument(attribute != UserHistoryAttribute.PASSWORD
                        || (oldValue == null && newValue == null),
                "Password change must not carry values");
    }
}
