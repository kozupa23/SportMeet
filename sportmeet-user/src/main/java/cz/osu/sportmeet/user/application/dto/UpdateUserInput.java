package cz.osu.sportmeet.user.application.dto;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Requested user changes; {@code null} values are left unchanged.
 *
 * @param displayName new visible name, or null to keep the current one
 * @param email       new e-mail address, or null to keep the current one
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record UpdateUserInput(@Nullable String displayName, @Nullable String email)
{
}
