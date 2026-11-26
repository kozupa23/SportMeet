package cz.osu.sportmeet.group.api;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.group.api.dto.CreateGroupRequest;
import cz.osu.sportmeet.group.api.dto.GroupDetailResponse;
import cz.osu.sportmeet.group.api.dto.GroupResponse;
import cz.osu.sportmeet.group.api.dto.UpdateGroupRequest;
import cz.osu.sportmeet.group.application.GroupFacade;
import cz.osu.sportmeet.group.application.dto.CreatedGroup;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.Principal;

/**
 * Exposes REST operations for sports groups.
 * The acting user is always taken from the authenticated session, never from the request.
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

        CreatedGroup created = groupFacade.createGroup(principal.getName(), GroupApiMapper.toInput(request));

        return ResponseEntity.created(URI.create("/api/v1/groups/" + created.id()))
                .body(GroupApiMapper.toResponse(created));
    }

    /**
     * Returns a group in the scope the signed-in user may see.
     *
     * @param id        group identifier
     * @param principal signed-in user
     * @return HTTP 200 response containing the group detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<GroupDetailResponse> getGroup(@PathVariable Long id, Principal principal)
    {
        return ResponseEntity.ok(GroupApiMapper.toResponse(groupFacade.getGroup(principal.getName(), id)));
    }

    /**
     * Changes the name, description and/or visibility of a group owned by the signed-in user.
     *
     * @param id        group identifier
     * @param request   requested changes with the version they are based on
     * @param principal signed-in user
     * @return HTTP 200 response containing the group after the change
     */
    @PatchMapping("/{id}")
    public ResponseEntity<GroupDetailResponse> updateGroup(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateGroupRequest request,
                                                           Principal principal)
    {
        Preconditions.checkNotNull(request, "request must not be null");

        return ResponseEntity.ok(GroupApiMapper.toResponse(
                groupFacade.updateGroup(principal.getName(), id, GroupApiMapper.toInput(request))));
    }
}
