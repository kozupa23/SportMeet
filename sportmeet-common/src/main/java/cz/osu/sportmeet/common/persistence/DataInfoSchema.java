package cz.osu.sportmeet.common.persistence;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NullMarked;

/**
 * Database column identifiers of the embeddable {@link DataInfo} type.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@UtilityClass
public class DataInfoSchema
{
    public static final String CREATED_AT = "CREATED_AT";
    public static final String CREATED_BY_OBJECT_ID = "CREATED_BY_OBJECT_ID";
    public static final String CREATED_BY_OBJECT_TYPE = "CREATED_BY_OBJECT_TYPE";
    public static final String MODIFIED_AT = "MODIFIED_AT";
    public static final String MODIFIED_BY_OBJECT_ID = "MODIFIED_BY_OBJECT_ID";
    public static final String MODIFIED_BY_OBJECT_TYPE = "MODIFIED_BY_OBJECT_TYPE";
}
