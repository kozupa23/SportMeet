package cz.osu.sportmeet.user.persistence.schema;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NullMarked;

/**
 * Database identifiers for per-attribute user change history.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@UtilityClass
public class UserHistorySchema
{
    public static final String TABLE_NAME = "T_USER_HISTORY";
    public static final String ID = "ID";
    public static final String USER_ID = "USER_ID";
    public static final String CHANGED_BY = "CHANGED_BY";
    public static final String CHANGED_BY_TYPE = "CHANGED_BY_TYPE";
    public static final String ATTRIBUTE_NAME = "ATTRIBUTE_NAME";
    public static final String OLD_VALUE = "OLD_VALUE";
    public static final String NEW_VALUE = "NEW_VALUE";
    public static final String CHANGED_AT = "CHANGED_AT";
}
