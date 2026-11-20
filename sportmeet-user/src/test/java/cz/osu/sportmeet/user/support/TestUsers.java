package cz.osu.sportmeet.user.support;

import cz.osu.sportmeet.user.domain.UserState;
import cz.osu.sportmeet.user.persistence.entity.User;
import org.jspecify.annotations.NullMarked;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;

/**
 * Builds user entities in a state that otherwise only persistence can produce.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public final class TestUsers
{
    public static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-11-10T00:00:00Z");

    private TestUsers()
    {
    }

    /**
     * Returns an active, valid user with a generated identifier.
     */
    public static User persisted(long id, String login, String displayName, String email)
    {
        User user = User.create(login, displayName, email, "encoded-password", CREATED_AT);
        ReflectionTestUtils.setField(user, "id", id);

        return user;
    }

    /**
     * Sets the persisted test user's user state.
     *
     * @param user  test user entity
     * @param state desired user state
     * @return the same test user
     */
    public static User withState(User user, UserState state)
    {
        ReflectionTestUtils.setField(user, "state", state);

        return user;
    }

    /**
     * Marks a persisted test user as invalid.
     *
     * @param user test user entity
     * @return the same test user
     */
    public static User invalid(User user)
    {
        ReflectionTestUtils.setField(user, "valid", false);

        return user;
    }
}
