package cz.osu.sportmeet.user.api;

import cz.osu.sportmeet.common.application.InvalidInputException;
import cz.osu.sportmeet.user.application.exception.ConcurrentUserModificationException;
import cz.osu.sportmeet.user.application.exception.DuplicateEmailException;
import cz.osu.sportmeet.user.application.exception.UserNotFoundException;
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
 * Stable error responses for current-user operations.
 * Only typed, client-caused exceptions are translated; anything else stays an HTTP 500.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@RestControllerAdvice(assignableTypes = CurrentUserController.class)
public final class CurrentUserExceptionHandler
{
    /**
     * Maps a missing or inactive user to HTTP 404.
     *
     * @param exception user lookup failure
     * @return HTTP 404 Problem Details response
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> missingUser(UserNotFoundException exception)
    {
        return problem(HttpStatus.NOT_FOUND, "User not found.");
    }

    /**
     * Maps an e-mail uniqueness conflict to HTTP 409.
     *
     * @param exception duplicate e-mail error
     * @return HTTP 409 Problem Details response
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ProblemDetail> emailConflict(DuplicateEmailException exception)
    {
        return problem(HttpStatus.CONFLICT, "Email conflicts with an existing user.");
    }

    /**
     * Reports a concurrent user update as HTTP 409.
     *
     * @param exception optimistic locking failure
     * @return HTTP 409 Problem Details response
     */
    @ExceptionHandler(ConcurrentUserModificationException.class)
    public ResponseEntity<ProblemDetail> concurrentModification(ConcurrentUserModificationException exception)
    {
        return problem(HttpStatus.CONFLICT, "The user was modified concurrently. Reload it and try again.");
    }

    /**
     * Rejects invalid values, malformed or unknown JSON and invalid paging parameters
     * without echoing submitted values.
     */
    @ExceptionHandler({InvalidInputException.class, MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetail> invalidInput(Exception exception)
    {
        return problem(HttpStatus.BAD_REQUEST, "Invalid user request.");
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail)
    {
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
    }
}
