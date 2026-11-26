package cz.osu.sportmeet.common.persistence;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
    SYSTEM;

    /**
     * Verifies that the actor identifier matches this actor category:
     * a USER actor requires an identifier, a SYSTEM actor must not have one.
     *
     * @param actorId identifier of the acting user, or null for SYSTEM
     * @throws IllegalArgumentException if the identifier does not match the category
     */
    public void validateActorId(@Nullable Long actorId)
    {
        Preconditions.checkArgument(this != USER || actorId != null,
                "A USER actor requires an identifier");
        Preconditions.checkArgument(this != SYSTEM || actorId == null,
                "A SYSTEM actor cannot have a user identifier");
    }
}