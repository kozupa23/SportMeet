package cz.osu.sportmeet.group.api.dto;

import cz.osu.sportmeet.group.domain.GroupVisibility;
import cz.osu.sportmeet.group.domain.MembershipRole;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * REST representation of a group detail in the scope the requesting user may see.
 *
 * @param id          group identifier
 * @param name        group name
 * @param description description, or null
 * @param visibility  group visibility
 * @param myRole      requesting user's role in the group, or null for a non-member;
 *                    meant for the UI only, every operation is authorized on the server again
 * @param members     current members ordered by role, or null when the user is not a member
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public record GroupDetailResponse(Long id, String name, @Nullable String description, GroupVisibility visibility,
                                  @Nullable MembershipRole myRole, @Nullable List<MemberResponse> members)
{
}
