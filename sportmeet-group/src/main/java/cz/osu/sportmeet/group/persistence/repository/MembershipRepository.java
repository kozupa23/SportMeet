package cz.osu.sportmeet.group.persistence.repository;

import cz.osu.sportmeet.group.domain.MembershipStatus;
import cz.osu.sportmeet.group.persistence.entity.Membership;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Provides persistence operations for group memberships.
 * Membership persistence is owned exclusively by the group module.
 * <p>
 * A membership is <em>current</em> when it is {@code ACTIVE} and technically valid;
 * ended or invalid memberships are history only.
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

    /**
     * Finds the current membership of a user in a group.
     *
     * @param groupId group identifier
     * @param userId  user identifier
     * @return current membership, or empty if the user is not a member
     */
    default Optional<Membership> findCurrentMembership(Long groupId, Long userId)
    {
        return findByGroupIdAndUserIdAndStatusAndValidTrue(groupId, userId, MembershipStatus.ACTIVE);
    }

    /**
     * Lists the current members of a group.
     *
     * @param groupId group identifier
     * @return current memberships, in no particular order
     */
    default List<Membership> findCurrentMembers(Long groupId)
    {
        return findByGroupIdAndStatusAndValidTrue(groupId, MembershipStatus.ACTIVE);
    }

    /**
     * Derived query behind {@link #findCurrentMembership(Long, Long)}; prefer that method.
     *
     * @param groupId group identifier
     * @param userId  user identifier
     * @param status  membership status
     * @return matching valid membership
     */
    Optional<Membership> findByGroupIdAndUserIdAndStatusAndValidTrue(Long groupId, Long userId,
                                                                      MembershipStatus status);

    /**
     * Derived query behind {@link #findCurrentMembers(Long)}; prefer that method.
     *
     * @param groupId group identifier
     * @param status  membership status
     * @return matching valid memberships
     */
    List<Membership> findByGroupIdAndStatusAndValidTrue(Long groupId, MembershipStatus status);
}
