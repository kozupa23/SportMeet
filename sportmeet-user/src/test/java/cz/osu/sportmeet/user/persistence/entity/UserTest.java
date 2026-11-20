package cz.osu.sportmeet.user.persistence.entity;

import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.user.domain.UserHistoryAttribute;
import cz.osu.sportmeet.user.domain.event.UserAttributeChanged;
import cz.osu.sportmeet.user.support.TestUsers;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies attribute changes, audit metadata and registered domain events of the user entity.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
class UserTest
{
    private static final OffsetDateTime CHANGED_AT = TestUsers.CREATED_AT.plusDays(1);

    @Test
    void changeDisplayNameUpdatesAuditAndRegistersEvent()
    {
        User user = TestUsers.persisted(7L, "runner", "Runner", "runner@example.com");

        assertThat(user.changeDisplayName("Fast Runner", CHANGED_AT, 7L, DataInfoObjectType.USER)).isTrue();

        assertThat(user.getDisplayName()).isEqualTo("Fast Runner");
        assertThat(user.getDataInfo().getModified()).contains(CHANGED_AT);
        assertThat(user.getDataInfo().getModifiedByObjectId()).contains(7L);
        assertThat(user.getDataInfo().getModifiedByObjectType()).contains(DataInfoObjectType.USER);
        assertThat(user.getDomainEvents()).containsExactly(new UserAttributeChanged(
                7L, UserHistoryAttribute.DISPLAY_NAME, "Runner", "Fast Runner",
                7L, DataInfoObjectType.USER, CHANGED_AT));
    }

    @Test
    void changeEmailRegistersEventWithPreviousValue()
    {
        User user = TestUsers.persisted(7L, "runner", "Runner", "runner@example.com");

        assertThat(user.changeEmail("new@example.com", CHANGED_AT, 7L, DataInfoObjectType.USER)).isTrue();

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getDomainEvents()).singleElement().satisfies(event ->
        {
            assertThat(event.attribute()).isEqualTo(UserHistoryAttribute.EMAIL);
            assertThat(event.oldValue()).isEqualTo("runner@example.com");
            assertThat(event.newValue()).isEqualTo("new@example.com");
        });
    }

    @Test
    void unchangedValuesDoNotModifyAuditNorRegisterEvents()
    {
        User user = TestUsers.persisted(7L, "runner", "Runner", "runner@example.com");

        assertThat(user.changeDisplayName("Runner", CHANGED_AT, 7L, DataInfoObjectType.USER)).isFalse();
        assertThat(user.changeEmail("runner@example.com", CHANGED_AT, 7L, DataInfoObjectType.USER)).isFalse();

        assertThat(user.getDataInfo().getModified()).isEmpty();
        assertThat(user.getDomainEvents()).isEmpty();
    }

    @Test
    void systemActorIsRecordedWithoutIdentifier()
    {
        User user = TestUsers.persisted(7L, "runner", "Runner", "runner@example.com");

        user.changeDisplayName("Renamed", CHANGED_AT, null, DataInfoObjectType.SYSTEM);

        assertThat(user.getDomainEvents()).singleElement().satisfies(event ->
        {
            assertThat(event.changedBy()).isNull();
            assertThat(event.changedByType()).isEqualTo(DataInfoObjectType.SYSTEM);
        });
    }

    /**
     * The actor rule itself is tested in {@code DataInfoTest}; here that a rejected change
     * leaves the entity untouched and registers no event.
     */
    @Test
    void inconsistentActorIsRejectedWithoutChangingState()
    {
        User user = TestUsers.persisted(7L, "runner", "Runner", "runner@example.com");

        assertThatThrownBy(() -> user.changeDisplayName("Renamed", CHANGED_AT, null, DataInfoObjectType.USER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> user.changeEmail("new@example.com", CHANGED_AT, 7L, DataInfoObjectType.SYSTEM))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(user.getDisplayName()).isEqualTo("Runner");
        assertThat(user.getEmail()).isEqualTo("runner@example.com");
        assertThat(user.getDataInfo().getModified()).isEmpty();
        assertThat(user.getDomainEvents()).isEmpty();
    }
}
