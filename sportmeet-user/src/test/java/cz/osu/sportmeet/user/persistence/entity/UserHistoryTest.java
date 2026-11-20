package cz.osu.sportmeet.user.persistence.entity;

import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.user.domain.UserHistoryAttribute;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies invariants of user history entries that the database also enforces.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
class UserHistoryTest
{
    private static final OffsetDateTime AT = OffsetDateTime.parse("2026-11-10T00:00:00Z");

    @Test
    void passwordHistoryMustNotContainValues()
    {
        assertThatThrownBy(() -> UserHistory.of(1L, UserHistoryAttribute.PASSWORD, "old-hash", null,
                1L, DataInfoObjectType.USER, AT)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> UserHistory.of(1L, UserHistoryAttribute.PASSWORD, null, "new-hash",
                1L, DataInfoObjectType.USER, AT)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void passwordHistoryWithoutValuesIsAccepted()
    {
        UserHistory entry = UserHistory.of(1L, UserHistoryAttribute.PASSWORD, null, null,
                1L, DataInfoObjectType.USER, AT);

        assertThat(entry.getOldValue()).isNull();
        assertThat(entry.getNewValue()).isNull();
    }

    /**
     * The actor rule itself is tested in {@code DataInfoTest}; here only that the factory applies it.
     */
    @Test
    void rejectsInconsistentActor()
    {
        assertThatThrownBy(() -> UserHistory.of(1L, UserHistoryAttribute.EMAIL, "a@example.com", "b@example.com",
                null, DataInfoObjectType.USER, AT)).isInstanceOf(IllegalArgumentException.class);
    }
}
