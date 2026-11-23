package cz.osu.sportmeet.group.application;

import cz.osu.sportmeet.group.application.dto.CreateGroupInput;
import cz.osu.sportmeet.group.application.dto.CreatedGroup;
import org.jspecify.annotations.NullMarked;

/**
 * Use cases for managing sports groups.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public interface GroupFacade
{
    /**
     * Founds a group with the signed-in user as its only active {@code OWNER}.
     * The group and the owner membership are stored atomically.
     *
     * @param login login of the signed-in user, taken from the session
     * @param input group attributes
     * @return information about the founded group
     * @throws cz.osu.sportmeet.group.application.exception.InactiveUserException if the user is not active
     * @throws cz.osu.sportmeet.common.application.InvalidInputException          if the group data is invalid
     */
    CreatedGroup createGroup(String login, CreateGroupInput input);
}
