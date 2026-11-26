package cz.osu.sportmeet.group.application.exception;

import org.jspecify.annotations.NullMarked;

/**
 * The group does not exist, has been invalidated, or the user may not see it.
 * These cases are intentionally indistinguishable so that the existence of private groups does not leak.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public final class GroupNotFoundException extends RuntimeException
{
    /**
     * Creates an error indicating that the group is not available to the user.
     */
    public GroupNotFoundException()
    {
        super("Group not found.");
    }
}
