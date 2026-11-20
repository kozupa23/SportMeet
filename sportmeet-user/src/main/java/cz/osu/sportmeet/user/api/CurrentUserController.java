package cz.osu.sportmeet.user.api;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.common.api.PageResponse;
import cz.osu.sportmeet.user.api.dto.HistoryEntryResponse;
import cz.osu.sportmeet.user.api.dto.UpdateUserRequest;
import cz.osu.sportmeet.user.api.dto.UserDetailResponse;
import cz.osu.sportmeet.user.application.CurrentUserFacade;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST access to the current user and their attribute history.
 * Returns only REST DTOs from {@code api.dto}; application types never leave this layer.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@RestController
@RequestMapping("/api/v1/users/me")
public final class CurrentUserController
{
    private final CurrentUserFacade currentUserFacade;

    /**
     * Initializes the controller with its current-user use-case contract.
     *
     * @param currentUserFacade application interface for the current user
     */
    public CurrentUserController(CurrentUserFacade currentUserFacade)
    {
        this.currentUserFacade = Preconditions.checkNotNull(currentUserFacade, "currentUserFacade must not be null");
    }

    /**
     * Returns the currently authenticated user, resolved from the security principal.
     */
    @GetMapping
    public ResponseEntity<UserDetailResponse> me(Principal principal)
    {
        return ResponseEntity.ok(UserApiMapper.toResponse(currentUserFacade.getCurrentUser(principal.getName())));
    }

    /**
     * Updates the display name and/or e-mail.
     */
    @PatchMapping
    public ResponseEntity<UserDetailResponse> update(@Valid @RequestBody UpdateUserRequest request,
                                                     Principal principal)
    {
        return ResponseEntity.ok(UserApiMapper.toResponse(
                currentUserFacade.updateCurrentUser(principal.getName(), UserApiMapper.toInput(request))));
    }

    /**
     * Lists the authenticated user's history, ordered newest first.
     */
    @GetMapping("/history")
    public ResponseEntity<PageResponse<HistoryEntryResponse>> history(Principal principal,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size)
    {
        return ResponseEntity.ok(PageResponse.from(
                currentUserFacade.getHistory(principal.getName(), page, size), UserApiMapper::toResponse));
    }
}
