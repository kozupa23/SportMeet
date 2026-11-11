package cz.osu.sportmeet.common.persistence;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NullMarked;

/**
 * Shared constants for database mappings.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@UtilityClass
public class DatabaseConstants
{
    public static final int STRING_COLUMN_MAX_LENGTH = 255;
    public static final int PASSWORD_COLUMN_MAX_LENGTH = 255;
}
