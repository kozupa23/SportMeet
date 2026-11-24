package cz.osu.sportmeet.user.api.lookup;

import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Public user-module contract that lets other modules identify users without touching
 * user internals (entities, repositories). The session principal carries only the login,
 * so modules that store user references (for example group memberships) resolve the ID here.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public interface UserLookupFacade
{
    /**
     * Finds the identifier of a user who may currently act in the application,
     * i.e. the user exists, is technically valid and is in the {@code ACTIVE} state.
     *
     * @param login login of the user; normalization is owned by the user module
     * @return user identifier, or empty if the user does not exist or is not active
     */
    Optional<Long> findActiveUserId(String login);

    /**
     * Returns public identification of the given users in a single query.
     * Users are returned regardless of their account state (for example blocked) and technical validity,
     * because their memberships and history still refer to them; unknown identifiers are omitted.
     *
     * @param ids user identifiers
     * @return summaries of the existing users, in no particular order
     */
    List<UserSummary> findUserSummaries(Collection<Long> ids);
}
