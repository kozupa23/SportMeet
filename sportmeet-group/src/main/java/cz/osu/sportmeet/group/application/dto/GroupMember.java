package cz.osu.sportmeet.group.application.dto;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.group.domain.MembershipRole;
import org.jspecify.annotations.NullMarked;

/**
 * Current member of a group with the information needed to identify them.
 * The login is deliberately omitted so that group members do not learn each other's login names.
 *
 * @param userId      member's user identifier
 * @param displayName member's visible name
 * @param role        member's current role in the group
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public record GroupMember(Long userId, String displayName, MembershipRole role)
{
    /**
     * Validates required values.
     *
     * @param userId      member's user identifier
     * @param displayName member's visible name
     * @param role        member's current role
     */
    public GroupMember
    {
        Preconditions.checkNotNull(userId, "userId must not be null");
        Preconditions.checkNotNull(displayName, "displayName must not be null");
        Preconditions.checkNotNull(role, "role must not be null");
    }
}
