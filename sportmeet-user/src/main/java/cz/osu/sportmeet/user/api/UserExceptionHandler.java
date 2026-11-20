package cz.osu.sportmeet.user.api;

import cz.osu.sportmeet.common.application.InvalidInputException;
import cz.osu.sportmeet.user.application.exception.DuplicateEmailException;
import cz.osu.sportmeet.user.application.exception.DuplicateLoginException;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
@NullMarked
@RestControllerAdvice(assignableTypes = UserController.class)
public final class UserExceptionHandler
{
    /**
     * Converts an existing email or login into an HTTP conflict.
     *
     * @param exception user uniqueness failure
     * @return HTTP 409 Problem Details response
     */
    @ExceptionHandler({DuplicateEmailException.class, DuplicateLoginException.class})
    public ResponseEntity<ProblemDetail> duplicateIdentity(RuntimeException exception)
    {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("User already exists");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Rejects invalid request fields without exposing submitted values.
     *
     * @return HTTP 400 Problem Details response
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ProblemDetail> invalidRequest()
    {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Registration data failed validation.");
        problem.setTitle("Invalid registration data");

        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Returns an HTTP 400 error for invalid business input.
     * Messages of {@link InvalidInputException} describe the violated rule, never the submitted value.
     *
     * @param exception invalid registration input
     * @return HTTP 400 Problem Details response
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ProblemDetail> invalidInput(InvalidInputException exception)
    {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid registration data");

        return ResponseEntity.badRequest().body(problem);
    }
}
