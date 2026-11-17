package cz.osu.sportmeet.api;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;

/**
 * Response containing a CSRF token for a browser client.
 *
 * @param headerName name of the required HTTP header
 * @param token      token value that must be supplied on a state-changing request
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record CsrfResponse(String headerName, String token)
{
    /**
     * Validates the response data.
     *
     * @param headerName CSRF header name
     * @param token      CSRF token value
     */
    public CsrfResponse
    {
        Preconditions.checkNotNull(headerName, "headerName must not be null");
        Preconditions.checkNotNull(token, "token must not be null");
    }
}
