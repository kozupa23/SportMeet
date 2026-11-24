package cz.osu.sportmeet.group.api.dto;

import cz.osu.sportmeet.group.domain.MembershipRole;
import org.jspecify.annotations.NullMarked;

/**
 * REST representation of a current group member.
 * Contains neither the login (half of the credentials) nor the e-mail.
 *
 * @param userId      member's user identifier
 * @param displayName member's visible name
 * @param role        member's current role in the group
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public record MemberResponse(Long userId, String displayName, MembershipRole role)
{
}
