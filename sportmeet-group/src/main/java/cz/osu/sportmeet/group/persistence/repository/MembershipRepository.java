package cz.osu.sportmeet.group.persistence.repository;

import cz.osu.sportmeet.group.persistence.entity.Membership;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Provides persistence operations for group memberships.
 * Membership persistence is owned exclusively by the group module.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public interface MembershipRepository extends JpaRepository<Membership, Long>
{
    /**
     * Lists all memberships of a group, including ended and invalid ones.
     *
     * @param groupId group identifier
     * @return memberships of the group
     */
    List<Membership> findByGroupId(Long groupId);
}
