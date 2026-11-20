package cz.osu.sportmeet.common.persistence;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies explicit audit initialization and modification rules without a database.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
class DataInfoTest
{
    private static final OffsetDateTime CREATED = OffsetDateTime.parse("2026-11-10T01:00:00Z");

    /**
     * Public self-registration is attributed to SYSTEM and has no modification yet.
     */
    @Test
    void systemCreationHasNoCreatorIdAndNoModification()
    {
        DataInfo info = DataInfo.forSystemCreation(CREATED);

        assertThat(info.getCreated()).isEqualTo(CREATED);
        assertThat(info.getCreatedByObjectType()).isEqualTo(DataInfoObjectType.SYSTEM);
        assertThat(info.getCreatedByObjectId()).isEmpty();
        assertThat(info.getModified()).isEmpty();
    }

    /**
     * Modification changes only modification metadata.
     */
    @Test
    void markModifiedPreservesCreator()
    {
        OffsetDateTime modified = CREATED.plusMinutes(10);
        DataInfo info = DataInfo.forCreation(CREATED, 42L, DataInfoObjectType.USER);

        info.markModified(modified, 51L, DataInfoObjectType.USER);

        assertThat(info.getCreated()).isEqualTo(CREATED);
        assertThat(info.getCreatedByObjectId()).contains(42L);
        assertThat(info.getCreatedByObjectType()).isEqualTo(DataInfoObjectType.USER);
        assertThat(info.getModified()).contains(modified);
        assertThat(info.getModifiedByObjectId()).contains(51L);
        assertThat(info.getModifiedByObjectType()).contains(DataInfoObjectType.USER);
    }

    /**
     * The actor rule shared by audit metadata and module histories:
     * USER requires an identifier, SYSTEM must not have one.
     */
    @Test
    void actorIdentifierMustMatchActorType()
    {
        assertThatThrownBy(() -> DataInfo.forCreation(CREATED, null, DataInfoObjectType.USER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DataInfo.forCreation(CREATED, 42L, DataInfoObjectType.SYSTEM))
                .isInstanceOf(IllegalArgumentException.class);

        DataInfo info = DataInfo.forSystemCreation(CREATED);
        assertThatThrownBy(() -> info.markModified(CREATED.plusMinutes(1), null, DataInfoObjectType.USER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Audit data cannot be moved backwards in time.
     */
    @Test
    void modificationCannotPrecedeCreation()
    {
        DataInfo info = DataInfo.forSystemCreation(CREATED);

        assertThatThrownBy(() -> info.markModified(CREATED.minusSeconds(1), null, DataInfoObjectType.SYSTEM))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
