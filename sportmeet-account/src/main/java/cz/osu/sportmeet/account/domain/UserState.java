package cz.osu.sportmeet.account.domain;

import org.jspecify.annotations.NullMarked;

/**
 * Account lifecycle state independent of technical validity.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public enum UserState
{
    ACTIVE,
    DISABLED,
    BLOCKED
}
