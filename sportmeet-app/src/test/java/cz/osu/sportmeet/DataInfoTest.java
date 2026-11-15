package cz.osu.sportmeet;

import cz.osu.sportmeet.common.persistence.DataInfo;
import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
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
class DataInfoTest
{
    /**
     * Verifies that registration through an invitation is attributed to SYSTEM,
     * not to the user who authored the invitation.
     */
    @Test
    void publicRegistrationHasSystemCreatorAndNoModification()
    {
        OffsetDateTime created = OffsetDateTime.parse("2026-11-10T01:00:00Z");
        DataInfo info = DataInfo.forSystemCreation(created);

        assertThat(info.getCreated()).isEqualTo(created);
        assertThat(info.getCreatedByObjectType()).isEqualTo(DataInfoObjectType.SYSTEM);
        assertThat(info.getCreatedByObjectId()).isEmpty();
        assertThat(info.getModified()).isEmpty();
    }

    /**
     * Verifies that modification changes only modification metadata.
     */
    @Test
    void markModifiedPreservesCreator()
    {
        Long creatorId = 42L;
        Long editorId = 51L;
        OffsetDateTime created = OffsetDateTime.parse("2026-11-10T01:00:00Z");
        OffsetDateTime modified = created.plusMinutes(10);
        DataInfo info = DataInfo.forCreation(created, creatorId, DataInfoObjectType.USER);
        info.markModified(modified, editorId, DataInfoObjectType.USER);

        assertThat(info.getCreated()).isEqualTo(created);
        assertThat(info.getCreatedByObjectId()).contains(creatorId);
        assertThat(info.getCreatedByObjectType()).isEqualTo(DataInfoObjectType.USER);
        assertThat(info.getModified()).contains(modified);
        assertThat(info.getModifiedByObjectId()).contains(editorId);
        assertThat(info.getModifiedByObjectType()).contains(DataInfoObjectType.USER);
    }

    /**
     * Verifies that user actors must have a concrete user ID.
     */
    @Test
    void userActorRequiresId()
    {
        OffsetDateTime now = OffsetDateTime.parse("2026-11-10T01:00:00Z");

        assertThatThrownBy(() -> DataInfo.forCreation(now, null, DataInfoObjectType.USER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Verifies that audit data cannot be moved backwards in time.
     */
    @Test
    void modificationCannotPrecedeCreation()
    {
        OffsetDateTime created = OffsetDateTime.parse("2026-11-10T01:00:00Z");
        DataInfo info = DataInfo.forSystemCreation(created);

        assertThatThrownBy(() -> info.markModified(created.minusSeconds(1), null, DataInfoObjectType.SYSTEM))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
