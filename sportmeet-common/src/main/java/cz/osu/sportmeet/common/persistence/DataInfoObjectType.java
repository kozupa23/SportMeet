package cz.osu.sportmeet.common.persistence;

import org.jspecify.annotations.NullMarked;

/**
 * Identifies the category of actor responsible for
 * creating or modifying a persistent entity.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public enum DataInfoObjectType
{
    USER,
    SYSTEM
}