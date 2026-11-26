package cz.osu.sportmeet.group.application;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.group.application.exception.InactiveUserException;
import cz.osu.sportmeet.group.domain.MembershipRole;
import cz.osu.sportmeet.group.persistence.entity.Membership;
import cz.osu.sportmeet.group.persistence.repository.MembershipRepository;
import cz.osu.sportmeet.user.api.lookup.UserLookupFacade;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Answers the two questions every group operation starts with: who is acting,
 * and which role does that user currently have in the group.
 * Decisions about what a role may do stay in the individual use cases.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@Component
public class GroupAccess
{
    private final UserLookupFacade userLookupFacade;
    private final MembershipRepository membershipRepository;

    /**
     * Creates the access helper.
     *
     * @param userLookupFacade     user-module contract resolving the signed-in user's identifier
     * @param membershipRepository repository for memberships
     */
    public GroupAccess(UserLookupFacade userLookupFacade, MembershipRepository membershipRepository)
    {
        this.userLookupFacade = Preconditions.checkNotNull(userLookupFacade, "userLookupFacade must not be null");
        this.membershipRepository = Preconditions.checkNotNull(
                membershipRepository, "membershipRepository must not be null");
    }

    /**
     * Resolves the identifier of the signed-in user; only users with an active account may work with groups.
     *
     * @param login login of the signed-in user, taken from the session
     * @return user identifier
     * @throws InactiveUserException if the account no longer exists or is not active
     */
    public Long activeUserId(String login)
    {
        Preconditions.checkNotNull(login, "login must not be null");

        return userLookupFacade.findActiveUserId(login).orElseThrow(InactiveUserException::new);
    }

    /**
     * Returns the role of the user's current membership in the group.
     *
     * @param groupId group identifier
     * @param userId  user identifier
     * @return current role, or empty if the user is not a current member
     */
    public Optional<MembershipRole> currentRole(Long groupId, Long userId)
    {
        Preconditions.checkNotNull(groupId, "groupId must not be null");
        Preconditions.checkNotNull(userId, "userId must not be null");

        return membershipRepository.findCurrentMembership(groupId, userId).map(Membership::getRole);
    }
}
