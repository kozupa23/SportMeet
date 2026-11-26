package cz.osu.sportmeet.group.api;

import cz.osu.sportmeet.common.application.InvalidInputException;
import cz.osu.sportmeet.group.application.exception.ConcurrentGroupModificationException;
import cz.osu.sportmeet.group.application.exception.GroupAccessDeniedException;
import cz.osu.sportmeet.group.application.exception.GroupNotFoundException;
import cz.osu.sportmeet.group.application.exception.InactiveUserException;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Maps group operation failures to stable REST Problem Details responses.
 * Only typed, client-caused exceptions are translated; anything else stays an HTTP 500.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@RestControllerAdvice(assignableTypes = GroupController.class)
public final class GroupExceptionHandler
{
    /**
     * Rejects a signed-in user whose account is no longer active.
     *
     * @param exception inactive user failure
     * @return HTTP 403 Problem Details response
     */
    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<ProblemDetail> inactiveUser(InactiveUserException exception)
    {
        return problem(HttpStatus.FORBIDDEN, "User account not active", exception.getMessage());
    }

    /**
     * Rejects an operation by a user who may see the group but lacks the required role.
     *
     * @param exception missing permission
     * @return HTTP 403 Problem Details response
     */
    @ExceptionHandler(GroupAccessDeniedException.class)
    public ResponseEntity<ProblemDetail> accessDenied(GroupAccessDeniedException exception)
    {
        return problem(HttpStatus.FORBIDDEN, "Operation not allowed", exception.getMessage());
    }

    /**
     * Reports a change based on an outdated version of the group, or a concurrent change.
     *
     * @param exception optimistic locking failure
     * @return HTTP 409 Problem Details response
     */
    @ExceptionHandler(ConcurrentGroupModificationException.class)
    public ResponseEntity<ProblemDetail> concurrentModification(ConcurrentGroupModificationException exception)
    {
        return problem(HttpStatus.CONFLICT, "Group modified concurrently", exception.getMessage());
    }

    /**
     * Reports a missing, invalidated or hidden group; the cases are indistinguishable on purpose.
     *
     * @param exception group lookup failure
     * @return HTTP 404 Problem Details response
     */
    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<ProblemDetail> groupNotFound(GroupNotFoundException exception)
    {
        return problem(HttpStatus.NOT_FOUND, "Group not found", exception.getMessage());
    }

    /**
     * Returns the violated business rule; messages of {@link InvalidInputException} never contain submitted values.
     *
     * @param exception invalid group data
     * @return HTTP 400 Problem Details response
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ProblemDetail> invalidInput(InvalidInputException exception)
    {
        return problem(HttpStatus.BAD_REQUEST, "Invalid group data", exception.getMessage());
    }

    /**
     * Rejects missing fields, malformed JSON, unknown properties and unknown enum values
     * without echoing submitted values.
     *
     * @return HTTP 400 Problem Details response
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ProblemDetail> invalidRequest()
    {
        return problem(HttpStatus.BAD_REQUEST, "Invalid group data", "Group data failed validation.");
    }

    /**
     * Rejects a group identifier in the URL that is not a number, for example {@code /api/v1/groups/abc}.
     *
     * @return HTTP 400 Problem Details response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> invalidIdentifier()
    {
        return problem(HttpStatus.BAD_REQUEST, "Invalid group request", "Group identifier must be a number.");
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail)
    {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);

        return ResponseEntity.status(status).body(problem);
    }
}
