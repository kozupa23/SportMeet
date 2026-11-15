package cz.osu.sportmeet.account.api;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.account.api.dto.CreateUserRequest;
import cz.osu.sportmeet.account.api.dto.UserResponse;
import cz.osu.sportmeet.account.application.UserFacade;
import cz.osu.sportmeet.account.application.dto.CreateUserInput;
import cz.osu.sportmeet.account.application.dto.CreatedUser;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Exposes REST operations for user account registration.
 * Delegates account creation to the account application facade.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@RestController
@RequestMapping("/api/v1/accounts")
public final class UserController
{
    private final UserFacade userFacade;

    /**
     * Initializes the controller with the account facade.
     *
     * @param userFacade application contract for account operations
     */
    public UserController(UserFacade userFacade)
    {
        this.userFacade = Preconditions.checkNotNull(
                userFacade, "userFacade must not be null");
    }

    /**
     * Registers a user account and returns its public representation.
     *
     * @param request validated account registration data
     * @return HTTP 201 response containing the created account
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request)
    {
        Preconditions.checkNotNull(request, "request must not be null");

        CreateUserInput input = new CreateUserInput(
                request.login(),
                request.displayName(),
                request.email(),
                request.password());

        CreatedUser created = userFacade.createUser(input);

        UserResponse response = new UserResponse(
                created.id(),
                created.login(),
                created.displayName(),
                created.email(),
                created.state());

        URI location = URI.create("/api/v1/accounts/" + created.id());

        return ResponseEntity.created(location).body(response);
    }
}