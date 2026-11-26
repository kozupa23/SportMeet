package cz.osu.sportmeet.group.domain;

import org.jspecify.annotations.NullMarked;

/**
 * Determines whether a sports group can be found by users who are not its members.
 * Independent of the group's technical validity.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public enum GroupVisibility
{
    /**
     * The group can be found and any user may ask to join it.
     */
    PUBLIC,

    /**
     * The group is visible only to its members; users join by invitation.
     */
    PRIVATE
}
