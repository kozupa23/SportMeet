package cz.osu.sportmeet.group.persistence.entity;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.common.persistence.DataInfo;
import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.common.persistence.MainDatabaseEntity;
import cz.osu.sportmeet.group.domain.MembershipRole;
import cz.osu.sportmeet.group.domain.MembershipStatus;
import cz.osu.sportmeet.group.persistence.schema.MembershipSchema;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * Membership of a user in a sports group. The join time is the creation time in {@link DataInfo}.
 * Ended memberships are kept as history; the user is referenced only by ID because users belong to the user module.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = MembershipSchema.TABLE_NAME)
public class Membership implements MainDatabaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = MembershipSchema.ID, nullable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    private @Nullable Long id;

    @Column(name = MembershipSchema.GROUP_ID, nullable = false, updatable = false)
    private Long groupId;

    @Column(name = MembershipSchema.USER_ID, nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = MembershipSchema.ROLE, nullable = false, length = 32)
    private MembershipRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = MembershipSchema.STATUS, nullable = false, length = 32)
    private MembershipStatus status;

    @Column(name = MembershipSchema.ENDED_AT)
    private @Nullable OffsetDateTime endedAt;

    @Column(name = MembershipSchema.VALID, nullable = false)
    private boolean valid;

    @Embedded
    private DataInfo dataInfo;

    @Version
    @Column(name = MembershipSchema.VERSION, nullable = false)
    private @Nullable Long version;

    /**
     * Creates the active {@code OWNER} membership of the user who founded the group.
     * The founder is also recorded as the creator of the membership.
     *
     * @param groupId   identifier of the already persisted group
     * @param userId    identifier of the founding user
     * @param createdAt time the group was founded
     * @return new active owner membership
     */
    public static Membership owner(Long groupId, Long userId, OffsetDateTime createdAt)
    {
        Preconditions.checkNotNull(groupId, "groupId must not be null");
        Preconditions.checkNotNull(userId, "userId must not be null");
        Preconditions.checkNotNull(createdAt, "createdAt must not be null");

        Membership membership = new Membership();
        membership.groupId = groupId;
        membership.userId = userId;
        membership.role = MembershipRole.OWNER;
        membership.status = MembershipStatus.ACTIVE;
        membership.valid = true;
        membership.dataInfo = DataInfo.forCreation(createdAt, userId, DataInfoObjectType.USER);

        return membership;
    }

    /**
     * Returns the database-generated primary key.
     *
     * @return persisted membership identifier
     * @throws IllegalStateException before persistence assigns an ID
     */
    @Override
    public Long getId()
    {
        Preconditions.checkState(id != null, "Membership ID has not been generated yet");

        return id;
    }
}
