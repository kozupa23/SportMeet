package cz.osu.sportmeet.group.application.dto;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Carries data for founding a group into the group application layer.
 * The values are validated and normalized before persistence.
 *
 * @param name        requested group name
 * @param description optional description
 * @param visibility  requested visibility, or null for the default
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public record CreateGroupInput(String name, @Nullable String description, @Nullable GroupVisibility visibility)
{
    /**
     * Validates that the required group name is present.
     *
     * @param name        requested group name
     * @param description optional description
     * @param visibility  requested visibility, or null for the default
     */
    public CreateGroupInput
    {
        Preconditions.checkNotNull(name, "name must not be null");
    }
}
