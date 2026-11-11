package cz.osu.sportmeet.common.persistence;

import com.google.common.base.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Embedded creation and last-modification audit metadata.
 * No revision history is stored here; Envers records historical entity versions.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Embeddable
public class DataInfo
{
    /**
     * -- GETTER --
     *
     * @return creation time
     */
    @Getter
    @Column(name = DataInfoSchema.CREATED_AT, nullable = false, updatable = false)
    private OffsetDateTime created;

    @Column(name = DataInfoSchema.CREATED_BY_OBJECT_ID, updatable = false)
    private @Nullable Long createdByObjectId;

    /**
     * -- GETTER --
     *
     * @return creator actor type
     */
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = DataInfoSchema.CREATED_BY_OBJECT_TYPE, nullable = false, updatable = false, length = 32)
    private DataInfoObjectType createdByObjectType;

    @Column(name = DataInfoSchema.MODIFIED_AT)
    private @Nullable OffsetDateTime modified;

    @Column(name = DataInfoSchema.MODIFIED_BY_OBJECT_ID)
    private @Nullable Long modifiedByObjectId;

    @Enumerated(EnumType.STRING)
    @Column(name = DataInfoSchema.MODIFIED_BY_OBJECT_TYPE, length = 32)
    private @Nullable DataInfoObjectType modifiedByObjectType;

    /**
     * Constructor used by JPA.
     */
    protected DataInfo()
    {
    }

    /**
     * Initializes audit metadata for a newly created entity.
     *
     * @param created             creation time
     * @param createdByObjectId   creator's user ID; null for SYSTEM
     * @param createdByObjectType actor category
     * @return initialized audit metadata
     */
    public static DataInfo forCreation(
            OffsetDateTime created,
            @Nullable Long createdByObjectId,
            DataInfoObjectType createdByObjectType)
    {
        Preconditions.checkNotNull(created, "created must not be null");
        Preconditions.checkNotNull(createdByObjectType, "createdByObjectType must not be null");
        validateActor(createdByObjectId, createdByObjectType);

        DataInfo dataInfo = new DataInfo();
        dataInfo.created = created;
        dataInfo.createdByObjectId = createdByObjectId;
        dataInfo.createdByObjectType = createdByObjectType;

        return dataInfo;
    }

    /**
     * Initializes audit metadata for public self-registration performed by SYSTEM.
     *
     * @param created creation time
     * @return initialized system audit metadata
     */
    public static DataInfo forSystemCreation(OffsetDateTime created)
    {
        Preconditions.checkNotNull(created, "created must not be null");

        return forCreation(created, null, DataInfoObjectType.SYSTEM);
    }

    /**
     * Marks the latest successful modification without replacing creation metadata.
     *
     * @param modified             modification time
     * @param modifiedByObjectId   modifying user's ID; null for SYSTEM
     * @param modifiedByObjectType actor category
     */
    public void markModified(
            OffsetDateTime modified,
            @Nullable Long modifiedByObjectId,
            DataInfoObjectType modifiedByObjectType)
    {
        Preconditions.checkNotNull(modified, "modified must not be null");
        Preconditions.checkNotNull(modifiedByObjectType, "modifiedByObjectType must not be null");
        validateActor(modifiedByObjectId, modifiedByObjectType);
        Preconditions.checkArgument(!modified.toInstant().isBefore(created.toInstant()),
                "Modification time cannot precede creation time");

        this.modified = modified;
        this.modifiedByObjectId = modifiedByObjectId;
        this.modifiedByObjectType = modifiedByObjectType;
    }

    private static void validateActor(@Nullable Long actorId, DataInfoObjectType actorType)
    {
        Preconditions.checkNotNull(actorType, "actorType must not be null");
        Preconditions.checkArgument(actorType != DataInfoObjectType.USER || actorId != null,
                "A USER actor requires an identifier");
        Preconditions.checkArgument(actorType != DataInfoObjectType.SYSTEM || actorId == null,
                "A SYSTEM actor cannot have a user identifier");
    }

    /**
     * @return creator ID, or empty for SYSTEM
     */
    public Optional<Long> getCreatedByObjectId()
    {
        return Optional.ofNullable(createdByObjectId);
    }

    /**
     * @return last modification time, if any
     */
    public Optional<OffsetDateTime> getModified()
    {
        return Optional.ofNullable(modified);
    }

    /**
     * @return last modifying user ID, if any
     */
    public Optional<Long> getModifiedByObjectId()
    {
        return Optional.ofNullable(modifiedByObjectId);
    }

    /**
     * @return last modifying actor type, if any
     */
    public Optional<DataInfoObjectType> getModifiedByObjectType()
    {
        return Optional.ofNullable(modifiedByObjectType);
    }
}
