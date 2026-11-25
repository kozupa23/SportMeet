package cz.osu.sportmeet.group.api.dto;

import cz.osu.sportmeet.group.domain.GroupVisibility;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Defines the request payload for founding a group.
 * Length limits and the default visibility are applied by the application validator.
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
}
