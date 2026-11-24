package cz.osu.sportmeet.group.domain;

import org.jspecify.annotations.NullMarked;

/**
 * Role of a member within a sports group, which determines what the member may manage.
 * Constants are declared from the most to the least privileged role; member lists are ordered this way.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public enum MembershipRole
{
    /**
     * Primary owner who manages the group; a group has exactly one active owner.
     */
    OWNER,

    /**
     * Co-owner who creates events, adds members and resolves membership requests;
     * a group may have several.
     */
    SECONDARY_OWNER,

    /**
     * Regular member who takes part in events, announcements and discussions.
     */
    MEMBER
}
