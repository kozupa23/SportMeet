package cz.osu.sportmeet.group.persistence.entity;

import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the initial state and invariants of a newly created group.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
class GroupTest
{
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-11-20T18:00:00Z");

    @Test
    void createsValidGroupWithFounderRecordedAsCreator()
    {
        Group group = Group.create("Florbal Ostrava", "Weekly training", GroupVisibility.PUBLIC, 7L, CREATED_AT);

        assertThat(group.getName()).isEqualTo("Florbal Ostrava");
        assertThat(group.getDescription()).isEqualTo("Weekly training");
        assertThat(group.getVisibility()).isEqualTo(GroupVisibility.PUBLIC);
        assertThat(group.isValid()).isTrue();
        assertThat(group.getDataInfo().getCreated()).isEqualTo(CREATED_AT);
        assertThat(group.getDataInfo().getCreatedByObjectType()).isEqualTo(DataInfoObjectType.USER);
        assertThat(group.getDataInfo().getCreatedByObjectId()).contains(7L);
        assertThat(group.getDataInfo().getModified()).isEmpty();
    }

    @Test
    void descriptionIsOptional()
    {
        Group group = Group.create("Florbal Ostrava", null, GroupVisibility.PRIVATE, 7L, CREATED_AT);

        assertThat(group.getDescription()).isNull();
    }

    @Test
    void acceptsNameOfMaximumLength()
    {
        String name = "x".repeat(Group.NAME_MAX_LENGTH);

        assertThat(Group.create(name, null, GroupVisibility.PRIVATE, 7L, CREATED_AT).getName()).isEqualTo(name);
    }

    /**
     * The application validator must reject these values first; the entity only guards its invariants.
     */
    @Test
    void rejectsValuesThatValidationMustHaveCaught()
    {
        assertThatThrownBy(() -> Group.create("   ", null, GroupVisibility.PUBLIC, 7L, CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Group.create("x".repeat(Group.NAME_MAX_LENGTH + 1), null,
                GroupVisibility.PUBLIC, 7L, CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Group.create("Florbal Ostrava", "   ", GroupVisibility.PUBLIC, 7L, CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void idIsUnavailableBeforePersistence()
    {
        Group group = Group.create("Florbal Ostrava", null, GroupVisibility.PUBLIC, 7L, CREATED_AT);

        assertThatThrownBy(group::getId).isInstanceOf(IllegalStateException.class);
    }
}
