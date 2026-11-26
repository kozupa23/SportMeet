package cz.osu.sportmeet.group.api;

import cz.osu.sportmeet.group.api.dto.CreateGroupRequest;
import cz.osu.sportmeet.group.api.dto.GroupDetailResponse;
import cz.osu.sportmeet.group.api.dto.GroupResponse;
import cz.osu.sportmeet.group.api.dto.MemberResponse;
import cz.osu.sportmeet.group.api.dto.UpdateGroupRequest;
import cz.osu.sportmeet.group.application.dto.CreateGroupInput;
import cz.osu.sportmeet.group.application.dto.CreatedGroup;
import cz.osu.sportmeet.group.application.dto.GroupDetail;
import cz.osu.sportmeet.group.application.dto.GroupMember;
import cz.osu.sportmeet.group.application.dto.UpdateGroupInput;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * Converts between the group REST contract and the application contract.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
final class GroupApiMapper
{
    private GroupApiMapper()
    {
    }

    static CreateGroupInput toInput(CreateGroupRequest request)
    {
        return new CreateGroupInput(request.name(), request.description(), request.visibility());
    }

    static GroupResponse toResponse(CreatedGroup group)
    {
        return new GroupResponse(group.id(), group.name(), group.description(), group.visibility());
    }

    static GroupDetailResponse toResponse(GroupDetail group)
    {
        List<GroupMember> members = group.members();

        return new GroupDetailResponse(group.id(), group.name(), group.description(), group.visibility(),
                group.version(), group.myRole(),
                members == null ? null : members.stream().map(GroupApiMapper::toResponse).toList());
    }

    static UpdateGroupInput toInput(UpdateGroupRequest request)
    {
        return new UpdateGroupInput(request.name(), request.description(), request.visibility(), request.version());
    }

    static MemberResponse toResponse(GroupMember member)
    {
        return new MemberResponse(member.userId(), member.displayName(), member.role());
    }
}
