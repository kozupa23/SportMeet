package cz.osu.sportmeet.group.persistence.schema;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NullMarked;

/**
 * Database table and column identifiers of the Group entity.
 * Audit columns come from the embedded {@link cz.osu.sportmeet.common.persistence.DataInfo}.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@UtilityClass
public class GroupSchema
{
    public static final String TABLE_NAME = "T_GROUP";
    public static final String ID = "ID";
    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String VISIBILITY = "VISIBILITY";
    public static final String VALID = "VALID";
    public static final String VERSION = "VERSION";
}
