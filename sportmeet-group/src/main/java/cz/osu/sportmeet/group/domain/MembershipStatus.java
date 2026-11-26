package cz.osu.sportmeet.group.domain;

import org.jspecify.annotations.NullMarked;

/**
 * Lifecycle state of a group membership, independent of the record's technical validity.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public enum MembershipStatus
{
    /**
     * The user is currently a member of the group.
     */
    ACTIVE,

    /**
     * The membership has ended; the record is kept as membership history.
     */
    ENDED
}
