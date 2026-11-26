package cz.osu.sportmeet.user.persistence.repository;

import cz.osu.sportmeet.user.persistence.entity.UserHistory;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Read and write access to per-user attribute history.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long>
{
    /**
     * Lists only history entries belonging to the requested user.
     */
    Page<UserHistory> findByUserId(Long userId, Pageable pageable);
}
