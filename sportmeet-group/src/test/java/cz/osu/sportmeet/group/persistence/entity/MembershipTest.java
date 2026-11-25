package cz.osu.sportmeet.group.persistence.entity;

import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.group.domain.MembershipRole;
import cz.osu.sportmeet.group.domain.MembershipStatus;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the initial state of the founder's owner membership.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
class MembershipTest
{
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-11-20T18:00:00Z");

    @Test
    void ownerMembershipIsActiveAndCreatedByFounder()
    {
        Membership membership = Membership.owner(3L, 7L, CREATED_AT);

        assertThat(membership.getGroupId()).isEqualTo(3L);
        assertThat(membership.getUserId()).isEqualTo(7L);
        assertThat(membership.getRole()).isEqualTo(MembershipRole.OWNER);
        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(membership.getEndedAt()).isNull();
        assertThat(membership.isValid()).isTrue();
        assertThat(membership.getDataInfo().getCreated()).isEqualTo(CREATED_AT);
        assertThat(membership.getDataInfo().getCreatedByObjectType()).isEqualTo(DataInfoObjectType.USER);
        assertThat(membership.getDataInfo().getCreatedByObjectId()).contains(7L);
    }

    @Test
    void idIsUnavailableBeforePersistence()
    {
        Membership membership = Membership.owner(3L, 7L, CREATED_AT);

        assertThatThrownBy(membership::getId).isInstanceOf(IllegalStateException.class);
    }
}
