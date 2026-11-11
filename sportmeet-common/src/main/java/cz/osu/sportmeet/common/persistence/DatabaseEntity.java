package cz.osu.sportmeet.common.persistence;

import org.jspecify.annotations.NullMarked;

/**
 * Common identity contract for entities with a generated numeric identifier.
 * Entities with composite keys are not required to implement this contract.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public interface DatabaseEntity
{
    /**
     * Returns the persisted entity identifier.
     *
     * @return generated database identifier
     * @throws IllegalStateException if the entity has not been persisted yet
     */
    Long getId();
}
