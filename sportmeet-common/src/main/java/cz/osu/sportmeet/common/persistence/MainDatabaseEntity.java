package cz.osu.sportmeet.common.persistence;

import org.jspecify.annotations.NullMarked;

/**
 * Identity and audit contract for main database entities.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public interface MainDatabaseEntity extends DatabaseEntity
{
    /**
     * Returns the entity's current embedded audit metadata.
     *
     * @return audit metadata
     */
    DataInfo getDataInfo();
}
