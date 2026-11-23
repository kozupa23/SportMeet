package cz.osu.sportmeet.group.application.dto;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Provides information about a newly founded group.
 *
 * @param id          identifier of the persisted group
 * @param name        normalized group name
 * @param description normalized description, or null
 * @param visibility  group visibility
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public record CreatedGroup(Long id, String name, @Nullable String description, GroupVisibility visibility)
{
    /**
     * Validates the required values of a created group.
     *
     * @param id          group identifier
     * @param name        group name
     * @param description description, or null
     * @param visibility  group visibility
     */
    public CreatedGroup
    {
        Preconditions.checkNotNull(id, "id must not be null");
        Preconditions.checkNotNull(name, "name must not be null");
        Preconditions.checkNotNull(visibility, "visibility must not be null");
    }
}
