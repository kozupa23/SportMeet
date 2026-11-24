package cz.osu.sportmeet.group.application.dto;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import cz.osu.sportmeet.group.domain.MembershipRole;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Information about a group in the scope the requesting user may see.
 *
 * @param id          group identifier
 * @param name        group name
 * @param description description, or null
 * @param visibility  group visibility
 * @param myRole      requesting user's current role, or null for a non-member
 * @param members     current members, or null when the user may not see them (non-member)
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public record GroupDetail(Long id, String name, @Nullable String description, GroupVisibility visibility,
                          @Nullable MembershipRole myRole, @Nullable List<GroupMember> members)
{
    /**
     * Validates required values and makes the member list immutable.
     *
     * @param id          group identifier
     * @param name        group name
     * @param description description, or null
     * @param visibility  group visibility
     * @param myRole      requesting user's role, or null
     * @param members     current members, or null
     */
    public GroupDetail
    {
        Preconditions.checkNotNull(id, "id must not be null");
        Preconditions.checkNotNull(name, "name must not be null");
        Preconditions.checkNotNull(visibility, "visibility must not be null");
        members = members == null ? null : List.copyOf(members);
    }
}
