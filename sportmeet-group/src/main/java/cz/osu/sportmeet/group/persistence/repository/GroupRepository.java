package cz.osu.sportmeet.group.persistence.repository;

import cz.osu.sportmeet.group.persistence.entity.Group;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides persistence operations for sports groups.
 * Group persistence is owned exclusively by the group module.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public interface GroupRepository extends JpaRepository<Group, Long>
{
}
