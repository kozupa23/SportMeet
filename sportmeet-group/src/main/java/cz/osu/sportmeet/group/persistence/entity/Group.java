package cz.osu.sportmeet.group.persistence.entity;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.common.persistence.DataInfo;
import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.common.persistence.MainDatabaseEntity;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import cz.osu.sportmeet.group.persistence.schema.GroupSchema;
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
 * Sports group with embedded audit metadata.
 * Members, including the owner, are separate {@link Membership} records that reference the group by its ID.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = GroupSchema.TABLE_NAME)
public class Group implements MainDatabaseEntity
{
    /**
     * Maximum length of a group name; matches the {@code T_GROUP.NAME} column.
     */
    public static final int NAME_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = GroupSchema.ID, nullable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    private @Nullable Long id;

    @Column(name = GroupSchema.NAME, nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(name = GroupSchema.DESCRIPTION, columnDefinition = "TEXT")
    private @Nullable String description;

    @Enumerated(EnumType.STRING)
    @Column(name = GroupSchema.VISIBILITY, nullable = false, length = 32)
    private GroupVisibility visibility;

    @Column(name = GroupSchema.VALID, nullable = false)
    private boolean valid;

    @Embedded
    private DataInfo dataInfo;

    @Version
    @Column(name = GroupSchema.VERSION, nullable = false)
    private @Nullable Long version;

    /**
     * Creates a technically valid group founded by a signed-in user.
     * Input values must already be validated and normalized by the application layer.
     *
     * @param name        normalized group name
     * @param description normalized description, or null if none was given
     * @param visibility  group visibility
     * @param creatorId   identifier of the founding user
     * @param createdAt   creation time
     * @return newly initialized group
     */
    public static Group create(String name, @Nullable String description, GroupVisibility visibility,
                               Long creatorId, OffsetDateTime createdAt)
    {
        Preconditions.checkNotNull(name, "name must not be null");
        Preconditions.checkArgument(!name.isBlank() && name.length() <= NAME_MAX_LENGTH,
                "Group name must have between 1 and %s characters.", NAME_MAX_LENGTH);
        Preconditions.checkArgument(description == null || !description.isBlank(),
                "description must be null or non-blank");
        Preconditions.checkNotNull(visibility, "visibility must not be null");
        Preconditions.checkNotNull(creatorId, "creatorId must not be null");
        Preconditions.checkNotNull(createdAt, "createdAt must not be null");

        Group group = new Group();
        group.name = name;
        group.description = description;
        group.visibility = visibility;
        group.valid = true;
        group.dataInfo = DataInfo.forCreation(createdAt, creatorId, DataInfoObjectType.USER);

        return group;
    }

    /**
     * Returns the database-generated primary key.
     *
     * @return persisted group identifier
     * @throws IllegalStateException before persistence assigns an ID
     */
    @Override
    public Long getId()
    {
        Preconditions.checkState(id != null, "Group ID has not been generated yet");

        return id;
    }
}
