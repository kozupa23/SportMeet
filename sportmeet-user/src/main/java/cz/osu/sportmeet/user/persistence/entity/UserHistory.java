package cz.osu.sportmeet.user.persistence.entity;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.user.domain.UserHistoryAttribute;
import cz.osu.sportmeet.user.persistence.schema.UserHistorySchema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * Immutable-by-convention history entry for one changed user attribute.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = UserHistorySchema.TABLE_NAME)
public class UserHistory
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = UserHistorySchema.ID, nullable = false, updatable = false)
    private @Nullable Long id;

    @Column(name = UserHistorySchema.USER_ID, nullable = false, updatable = false)
    private Long userId;

    @Column(name = UserHistorySchema.CHANGED_BY, updatable = false)
    private @Nullable Long changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = UserHistorySchema.CHANGED_BY_TYPE, nullable = false, length = 32, updatable = false)
    private DataInfoObjectType changedByType;

    @Enumerated(EnumType.STRING)
    @Column(name = UserHistorySchema.ATTRIBUTE_NAME, nullable = false, length = 100, updatable = false)
    private UserHistoryAttribute attributeName;

    @Column(name = UserHistorySchema.OLD_VALUE, columnDefinition = "TEXT", updatable = false)
    private @Nullable String oldValue;

    @Column(name = UserHistorySchema.NEW_VALUE, columnDefinition = "TEXT", updatable = false)
    private @Nullable String newValue;

    @Column(name = UserHistorySchema.CHANGED_AT, nullable = false, updatable = false)
    private OffsetDateTime changedAt;

    /**
     * Creates history of a single changed attribute.
     * A USER actor requires an identifier, a SYSTEM actor must not have one.
     * Password changes must always omit both values.
     */
    public static UserHistory of(Long userId, UserHistoryAttribute attribute,
                                 @Nullable String oldValue, @Nullable String newValue,
                                 @Nullable Long changedBy, DataInfoObjectType changedByType,
                                 OffsetDateTime at)
    {
        Preconditions.checkNotNull(userId, "userId must not be null");
        Preconditions.checkNotNull(attribute, "attribute must not be null");
        Preconditions.checkNotNull(changedByType, "changedByType must not be null");
        Preconditions.checkNotNull(at, "at must not be null");
        changedByType.validateActorId(changedBy);
        Preconditions.checkArgument(attribute != UserHistoryAttribute.PASSWORD ||
                        (oldValue == null && newValue == null),
                "Password history must not contain values");

        UserHistory entry = new UserHistory();
        entry.userId = userId;
        entry.changedBy = changedBy;
        entry.changedByType = changedByType;
        entry.attributeName = attribute;
        entry.oldValue = oldValue;
        entry.newValue = newValue;
        entry.changedAt = at;

        return entry;
    }
}
