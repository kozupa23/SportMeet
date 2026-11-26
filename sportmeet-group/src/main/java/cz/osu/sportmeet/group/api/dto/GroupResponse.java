package cz.osu.sportmeet.group.api.dto;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * REST representation of a group. Does not expose internal validity or audit information.
 *
 * @param id          group identifier
 * @param name        group name
 * @param description description, or null
 * @param visibility  group visibility
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public record GroupResponse(Long id, String name, @Nullable String description, GroupVisibility visibility)
{
    /**
     * Validates required response attributes.
     *
     * @param id          group identifier
     * @param name        group name
     * @param description description, or null
     * @param visibility  group visibility
     */
    public GroupResponse
    {
        Preconditions.checkNotNull(id, "id must not be null");
        Preconditions.checkNotNull(name, "name must not be null");
        Preconditions.checkNotNull(visibility, "visibility must not be null");
    }
}
