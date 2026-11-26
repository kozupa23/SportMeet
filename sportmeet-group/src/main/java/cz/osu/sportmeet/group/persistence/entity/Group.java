package cz.osu.sportmeet.group.persistence.entity;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.common.persistence.DataInfo;
import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.common.persistence.MainDatabaseEntity;
import cz.osu.sportmeet.group.domain.GroupHistoryAttribute;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import cz.osu.sportmeet.group.domain.event.GroupAttributeChanged;
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
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Sports group with embedded audit metadata.
 * Members, including the owner, are separate {@link Membership} records that reference the group by its ID.
 * <p>
 * Every change of a tracked attribute registers a {@link GroupAttributeChanged} domain event.
 * Spring Data publishes pending events when the entity is passed to a repository {@code save*}
 * method, therefore callers changing a group must always save it through {@code GroupRepository}.
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

    @Transient
    @Getter(AccessLevel.NONE)
    private final List<GroupAttributeChanged> domainEvents = new ArrayList<>();

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
        checkName(name);
        checkDescription(description);
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

    /**
     * Changes the name when it actually differs, updates modification metadata
     * and registers a {@link GroupAttributeChanged} event.
     *
     * @param newName       validated, normalized name
     * @param changedAt     UTC timestamp
     * @param changedBy     identifier of the acting user; null for SYSTEM
     * @param changedByType category of the actor
     * @return true if the name changed
     */
    public boolean changeName(String newName, OffsetDateTime changedAt,
                              @Nullable Long changedBy, DataInfoObjectType changedByType)
    {
        checkName(newName);
        if (name.equals(newName))
        {
            return false;
        }

        String previous = name;
        dataInfo.markModified(changedAt, changedBy, changedByType);
        name = newName;
        registerChange(GroupHistoryAttribute.NAME, previous, newName, changedAt, changedBy, changedByType);

        return true;
    }

    /**
     * Changes or removes the description when it actually differs, updates modification metadata
     * and registers a {@link GroupAttributeChanged} event.
     *
     * @param newDescription validated, normalized description, or null to remove it
     * @param changedAt      UTC timestamp
     * @param changedBy      identifier of the acting user; null for SYSTEM
     * @param changedByType  category of the actor
     * @return true if the description changed
     */
    public boolean changeDescription(@Nullable String newDescription, OffsetDateTime changedAt,
                                     @Nullable Long changedBy, DataInfoObjectType changedByType)
    {
        checkDescription(newDescription);
        if (Objects.equals(description, newDescription))
        {
            return false;
        }

        String previous = description;
        dataInfo.markModified(changedAt, changedBy, changedByType);
        description = newDescription;
        registerChange(GroupHistoryAttribute.DESCRIPTION, previous, newDescription, changedAt, changedBy,
                changedByType);

        return true;
    }

    /**
     * Changes the visibility when it actually differs, updates modification metadata
     * and registers a {@link GroupAttributeChanged} event.
     *
     * @param newVisibility new visibility
     * @param changedAt     UTC timestamp
     * @param changedBy     identifier of the acting user; null for SYSTEM
     * @param changedByType category of the actor
     * @return true if the visibility changed
     */
    public boolean changeVisibility(GroupVisibility newVisibility, OffsetDateTime changedAt,
                                    @Nullable Long changedBy, DataInfoObjectType changedByType)
    {
        Preconditions.checkNotNull(newVisibility, "newVisibility must not be null");
        if (visibility == newVisibility)
        {
            return false;
        }

        GroupVisibility previous = visibility;
        dataInfo.markModified(changedAt, changedBy, changedByType);
        visibility = newVisibility;
        registerChange(GroupHistoryAttribute.VISIBILITY, previous.name(), newVisibility.name(), changedAt,
                changedBy, changedByType);

        return true;
    }

    /**
     * Returns events registered since the last publication.
     * Invoked by Spring Data after {@code save*}; also useful for verifying domain behaviour.
     *
     * @return immutable snapshot of pending events
     */
    @DomainEvents
    public List<GroupAttributeChanged> getDomainEvents()
    {
        return List.copyOf(domainEvents);
    }

    /**
     * Discards events after Spring Data has published them.
     */
    @AfterDomainEventPublication
    void clearDomainEvents()
    {
        domainEvents.clear();
    }

    private void registerChange(GroupHistoryAttribute attribute, @Nullable String oldValue,
                                @Nullable String newValue, OffsetDateTime changedAt, @Nullable Long changedBy,
                                DataInfoObjectType changedByType)
    {
        domainEvents.add(new GroupAttributeChanged(
                getId(), attribute, oldValue, newValue, changedBy, changedByType, changedAt));
    }

    private static void checkName(String name)
    {
        Preconditions.checkNotNull(name, "name must not be null");
        Preconditions.checkArgument(!name.isBlank() && name.length() <= NAME_MAX_LENGTH,
                "Group name must have between 1 and %s characters.", NAME_MAX_LENGTH);
    }

    private static void checkDescription(@Nullable String description)
    {
        Preconditions.checkArgument(description == null || !description.isBlank(),
                "description must be null or non-blank");
    }
}
