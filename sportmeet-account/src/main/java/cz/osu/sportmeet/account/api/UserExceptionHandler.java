package cz.osu.sportmeet.account.api;

import cz.osu.sportmeet.account.application.exception.DuplicateEmailException;
import cz.osu.sportmeet.account.application.exception.DuplicateLoginException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps registration failures to stable REST Problem Details responses.
 * Internal database errors and submitted credentials are never disclosed.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@RestControllerAdvice(assignableTypes = UserController.class)
public final class UserExceptionHandler
{
    /**
     * Converts an existing email or login into an HTTP conflict.
     *
     * @param exception account uniqueness failure
     * @return HTTP 409 Problem Details response
     */
    @ExceptionHandler({DuplicateEmailException.class, DuplicateLoginException.class})
    public ResponseEntity<ProblemDetail> duplicateIdentity(RuntimeException exception)
    {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Account already exists");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Handles uniqueness races detected by database constraints during an insert.
     *
     * @param exception database constraint violation
     * @return HTTP 409 response without database-specific error details
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> constraintConflict(DataIntegrityViolationException exception)
    {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The account conflicts with existing data.");
        problem.setTitle("Account registration conflict");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Rejects invalid request fields without exposing submitted values.
     *
     * @return HTTP 400 Problem Details response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> invalidRequest()
    {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Registration data failed validation.");
        problem.setTitle("Invalid registration data");

        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Returns an HTTP 400 error for invalid business input.
     *
     * @param exception invalid registration input
     * @return HTTP 400 Problem Details response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> invalidInput(IllegalArgumentException exception)
    {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid registration data");

        return ResponseEntity.badRequest().body(problem);
    }
}
