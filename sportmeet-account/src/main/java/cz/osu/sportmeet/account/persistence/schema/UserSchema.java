package cz.osu.sportmeet.account.persistence.schema;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NullMarked;

/**
 * Database table and column identifiers of the User entity.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@UtilityClass
public class UserSchema
{
    public static final String TABLE_NAME = "T_USER";
    public static final String ID = "ID";
    public static final String LOGIN = "LOGIN";
    public static final String DISPLAY_NAME = "DISPLAY_NAME";
    public static final String EMAIL = "EMAIL";
    public static final String PASSWORD_HASH = "PASSWORD_HASH";
    public static final String STATE = "STATE";
    public static final String VALID = "VALID";
    public static final String VERSION = "VERSION";
}
