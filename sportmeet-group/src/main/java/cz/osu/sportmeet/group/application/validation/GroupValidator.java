package cz.osu.sportmeet.group.application.validation;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.common.application.InvalidInputException;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import cz.osu.sportmeet.group.persistence.entity.Group;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Validates and normalizes group data.
 * Applies the same business rules independently of the API through which a group is created or changed.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@Component
public final class GroupValidator
{
    /**
     * Maximum length of a group description. The database column is unbounded, so this is the only limit.
     */
    public static final int DESCRIPTION_MAX_LENGTH = 2000;

    /**
     * Visibility used when the client does not choose one.
     */
    public static final GroupVisibility DEFAULT_VISIBILITY = GroupVisibility.PRIVATE;

    /**
     * Strips and validates a group name.
     *
     * @param name submitted group name
     * @return stripped group name
     * @throws InvalidInputException if the name is blank or too long
     */
    public String normalizeName(String name)
    {
        Preconditions.checkNotNull(name, "name must not be null");

        String normalized = name.strip();
        InvalidInputException.check(!normalized.isEmpty() && normalized.length() <= Group.NAME_MAX_LENGTH,
                "Group name must have between 1 and " + Group.NAME_MAX_LENGTH + " characters.");

        return normalized;
    }

    /**
     * Strips and validates an optional description; a blank description is treated as no description.
     *
     * @param description submitted description, or null
     * @return stripped description, or null if none was given
     * @throws InvalidInputException if the description is too long
     */
    public @Nullable String normalizeDescription(@Nullable String description)
    {
        if (description == null)
        {
            return null;
        }

        String normalized = description.strip();
        InvalidInputException.check(normalized.length() <= DESCRIPTION_MAX_LENGTH,
                "Group description must have at most " + DESCRIPTION_MAX_LENGTH + " characters.");

        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Returns the requested visibility, or the default when none was requested.
     *
     * @param visibility submitted visibility, or null
     * @return visibility to store
     */
    public GroupVisibility resolveVisibility(@Nullable GroupVisibility visibility)
    {
        return visibility == null ? DEFAULT_VISIBILITY : visibility;
    }
}
