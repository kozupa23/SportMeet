package cz.osu.sportmeet.user.api.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Optional user properties to change; omitted properties remain unchanged.
 * Unknown properties (for example {@code state} or {@code login}) are rejected
 * so that clients cannot assume they were applied.
 *
 * @param displayName new visible name
 * @param email       new e-mail address
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record UpdateUserRequest(
        @Nullable @Size(max = 255) String displayName,
        @Nullable @Size(max = 254) String email)
{
    /**
     * Fails deserialization for any property not declared by this request.
     *
     * @param name  unknown property name
     * @param value ignored value
     * @throws IllegalArgumentException always; reported to the client as HTTP 400
     */
    @JsonAnySetter
    void rejectUnknownProperty(String name, @Nullable Object value)
    {
        throw new IllegalArgumentException("Unknown user property.");
    }
}
