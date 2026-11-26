package cz.osu.sportmeet.user.application;

import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.user.domain.UserHistoryAttribute;
import cz.osu.sportmeet.user.domain.event.UserAttributeChanged;
import cz.osu.sportmeet.user.persistence.entity.UserHistory;
import cz.osu.sportmeet.user.persistence.repository.UserHistoryRepository;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Verifies that a domain event is translated into exactly one history entry.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
class UserHistoryRecorderTest
{
    @Test
    void persistsOneEntryPerEvent()
    {
        UserHistoryRepository history = mock(UserHistoryRepository.class);
        OffsetDateTime at = OffsetDateTime.parse("2026-11-11T00:00:00Z");

        new UserHistoryRecorder(history).on(new UserAttributeChanged(7L, UserHistoryAttribute.DISPLAY_NAME,
                "Runner", "Fast Runner", null, DataInfoObjectType.SYSTEM, at));

        ArgumentCaptor<UserHistory> saved = ArgumentCaptor.forClass(UserHistory.class);
        verify(history).save(saved.capture());
        assertThat(saved.getValue())
                .extracting(UserHistory::getUserId, UserHistory::getAttributeName, UserHistory::getOldValue,
                        UserHistory::getNewValue, UserHistory::getChangedBy, UserHistory::getChangedByType,
                        UserHistory::getChangedAt)
                .containsExactly(7L, UserHistoryAttribute.DISPLAY_NAME, "Runner", "Fast Runner", null,
                        DataInfoObjectType.SYSTEM, at);
    }
}
