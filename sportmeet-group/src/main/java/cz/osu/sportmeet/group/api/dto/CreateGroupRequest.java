package cz.osu.sportmeet.group.api.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Defines the request payload for founding a group.
 * Length limits and the default visibility are applied by the application validator.
 * Unknown properties (for example a misspelled {@code visiblity} or an {@code ownerId}) are rejected,
 * so that clients cannot assume they were applied.
 *
 * @param name        group name
 * @param description optional description
 * @param visibility  {@code PUBLIC} or {@code PRIVATE}; {@code PRIVATE} when omitted
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public record CreateGroupRequest(
        @NotBlank String name,
        @Nullable String description,
        @Nullable GroupVisibility visibility)
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
        throw new IllegalArgumentException("Unknown group property.");
    }
}
