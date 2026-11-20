package cz.osu.sportmeet.user.api;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.user.api.dto.CreateUserRequest;
import cz.osu.sportmeet.user.api.dto.UserResponse;
import cz.osu.sportmeet.user.application.UserFacade;
import cz.osu.sportmeet.user.application.dto.CreateUserInput;
import cz.osu.sportmeet.user.application.dto.CreatedUser;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Exposes REST operations for user registration.
 * Delegates user creation to the user application facade.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@RestController
@RequestMapping("/api/v1/users")
public final class UserController
{
    private final UserFacade userFacade;

    /**
     * Initializes the controller with the user facade.
     *
     * @param userFacade application contract for user operations
     */
    public UserController(UserFacade userFacade)
    {
        this.userFacade = Preconditions.checkNotNull(
                userFacade, "userFacade must not be null");
    }

    /**
     * Registers a user and returns its public representation.
     *
     * @param request validated user registration data
     * @return HTTP 201 response containing the created user
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

        URI location = URI.create("/api/v1/users/" + created.id());

        return ResponseEntity.created(location).body(response);
    }
}