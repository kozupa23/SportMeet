package cz.osu.sportmeet.user.application;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.user.domain.event.UserAttributeChanged;
import cz.osu.sportmeet.user.persistence.entity.UserHistory;
import cz.osu.sportmeet.user.persistence.repository.UserHistoryRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists every user attribute change into {@code T_USER_HISTORY}.
 * Runs synchronously inside the transaction that changed the user, so the change
 * and its history are committed or rolled back together.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Component
public class UserHistoryRecorder
{
    private final UserHistoryRepository userHistoryRepository;

    /**
     * Creates a listener that persists changes to user history.
     *
     * @param userHistoryRepository repository for user history entries
     */
    public UserHistoryRecorder(UserHistoryRepository userHistoryRepository)
    {
        this.userHistoryRepository = Preconditions.checkNotNull(
                userHistoryRepository, "userHistoryRepository must not be null");
    }

    /**
     * Records one changed attribute.
     *
     * @param event change raised by the user entity
     */
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(UserAttributeChanged event)
    {
        Preconditions.checkNotNull(event, "event must not be null");

        userHistoryRepository.save(UserHistory.of(event.userId(), event.attribute(), event.oldValue(), event.newValue(),
                event.changedBy(), event.changedByType(), event.changedAt()));
    }
}
