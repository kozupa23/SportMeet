package cz.osu.sportmeet.group.api;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.group.api.dto.CreateGroupRequest;
import cz.osu.sportmeet.group.api.dto.GroupResponse;
import cz.osu.sportmeet.group.application.GroupFacade;
import cz.osu.sportmeet.group.application.dto.CreateGroupInput;
import cz.osu.sportmeet.group.application.dto.CreatedGroup;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.Principal;

/**
 * Exposes REST operations for sports groups.
 * The acting user is always taken from the authenticated session, never from the request body.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@RestController
@RequestMapping("/api/v1/groups")
public final class GroupController
{
    private final GroupFacade groupFacade;

    /**
     * Initializes the controller with the group facade.
     *
     * @param groupFacade application contract for group operations
     */
    public GroupController(GroupFacade groupFacade)
    {
        this.groupFacade = Preconditions.checkNotNull(groupFacade, "groupFacade must not be null");
    }

    /**
     * Founds a group owned by the signed-in user.
     *
     * @param request   validated group data
     * @param principal signed-in user
     * @return HTTP 201 response containing the founded group
     */
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request,
                                                     Principal principal)
    {
        Preconditions.checkNotNull(request, "request must not be null");

        CreatedGroup created = groupFacade.createGroup(principal.getName(),
                new CreateGroupInput(request.name(), request.description(), request.visibility()));

        GroupResponse response = new GroupResponse(created.id(), created.name(), created.description(),
                created.visibility());

        return ResponseEntity.created(URI.create("/api/v1/groups/" + created.id())).body(response);
    }
}
