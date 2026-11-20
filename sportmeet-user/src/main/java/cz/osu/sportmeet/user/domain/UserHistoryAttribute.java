package cz.osu.sportmeet.user.domain;

import org.jspecify.annotations.NullMarked;

/**
 * Attributes whose modification can be recorded in user history.
 * Values of secret attributes must never be persisted in history.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public enum UserHistoryAttribute
{
    DISPLAY_NAME,
    EMAIL,
    PASSWORD,
    STATE
}
