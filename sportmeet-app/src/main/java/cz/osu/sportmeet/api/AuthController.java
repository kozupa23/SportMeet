package cz.osu.sportmeet.api;

import com.google.common.base.Preconditions;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Supplies the CSRF token and current authentication state to the SPA client.
 * Login and logout are handled by Spring Security filters rather than this controller.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@RestController
@RequestMapping("/api/v1/auth")
public final class AuthController
{
    /**
     * Returns the CSRF token needed by the client for state-changing requests.
     *
     * @param request current servlet request
     * @return CSRF header name and token, without caching
     */
    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> csrf(HttpServletRequest request)
    {
        Preconditions.checkNotNull(request, "request must not be null");
        CsrfToken token = (CsrfToken) Preconditions.checkNotNull(
                request.getAttribute(CsrfToken.class.getName()), "CSRF token is unavailable");

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new CsrfResponse(token.getHeaderName(), token.getToken()));
    }

    /**
     * Returns the authenticated user's canonical login.
     *
     * @param authentication current authenticated principal
     * @return current login without credential material
     */
    @GetMapping("/session")
    public SessionResponse session(Authentication authentication)
    {
        Preconditions.checkNotNull(authentication, "authentication must not be null");

        return new SessionResponse(authentication.getName());
    }
}
