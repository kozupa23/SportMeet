package cz.osu.sportmeet.group.persistence.schema;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NullMarked;

/**
 * Database table and column identifiers of the Membership entity.
 * Audit columns come from the embedded {@link cz.osu.sportmeet.common.persistence.DataInfo}.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@UtilityClass
public class MembershipSchema
{
    public static final String TABLE_NAME = "T_GROUP_MEMBERSHIP";
    public static final String ID = "ID";
    public static final String GROUP_ID = "GROUP_ID";
    public static final String USER_ID = "USER_ID";
    public static final String ROLE = "ROLE";
    public static final String STATUS = "STATUS";
    public static final String ENDED_AT = "ENDED_AT";
    public static final String VALID = "VALID";
    public static final String VERSION = "VERSION";
}
