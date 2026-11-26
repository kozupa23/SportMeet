package cz.osu.sportmeet.group.application;

import cz.osu.sportmeet.group.application.dto.CreateGroupInput;
import cz.osu.sportmeet.group.application.dto.CreatedGroup;
import cz.osu.sportmeet.group.application.dto.GroupDetail;
import cz.osu.sportmeet.group.application.dto.UpdateGroupInput;
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

    /**
     * Returns a valid group in the scope the signed-in user may see: current members see the group
     * with its current members, non-members see only basic information of a public group.
     *
     * @param login   login of the signed-in user, taken from the session
     * @param groupId group identifier
     * @return group information
     * @throws cz.osu.sportmeet.group.application.exception.InactiveUserException  if the user is not active
     * @throws cz.osu.sportmeet.group.application.exception.GroupNotFoundException if the group does not exist,
     *                                                                             is invalidated or is private
     *                                                                             and the user is not a member
     */
    GroupDetail getGroup(String login, Long groupId);

    /**
     * Changes the name, description and/or visibility of a valid group; only its {@code OWNER} may do so.
     * Every actual change is recorded in the group history in the same transaction.
     *
     * @param login   login of the signed-in user, taken from the session
     * @param groupId group identifier
     * @param input   requested changes and the version they are based on
     * @return group information after the change
     * @throws cz.osu.sportmeet.group.application.exception.InactiveUserException                if the user is
     *                                                                                           not active
     * @throws cz.osu.sportmeet.group.application.exception.GroupNotFoundException               if the group
     *                                                                                           does not exist,
     *                                                                                           is invalidated
     *                                                                                           or is private
     *                                                                                           and the user is
     *                                                                                           not a member
     * @throws cz.osu.sportmeet.group.application.exception.GroupAccessDeniedException           if the user is
     *                                                                                           not the owner
     * @throws cz.osu.sportmeet.common.application.InvalidInputException                         if nothing is
     *                                                                                           requested or a
     *                                                                                           value is invalid
     * @throws cz.osu.sportmeet.group.application.exception.ConcurrentGroupModificationException if the group
     *                                                                                           changed after
     *                                                                                           the client read it
     */
    GroupDetail updateGroup(String login, Long groupId, UpdateGroupInput input);
}
