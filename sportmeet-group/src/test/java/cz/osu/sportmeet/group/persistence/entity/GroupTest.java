package cz.osu.sportmeet.group.persistence.entity;

import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.group.domain.GroupHistoryAttribute;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import cz.osu.sportmeet.group.domain.event.GroupAttributeChanged;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

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
    private static final OffsetDateTime CHANGED_AT = CREATED_AT.plusDays(1);
    private static final long GROUP_ID = 3L;

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

    // ---------------------------------------------------------------- changes (US-04-03)

    @Test
    void changeNameUpdatesAuditAndRegistersEvent()
    {
        Group group = persisted();

        assertThat(group.changeName("Florbal Poruba", CHANGED_AT, 7L, DataInfoObjectType.USER)).isTrue();

        assertThat(group.getName()).isEqualTo("Florbal Poruba");
        assertThat(group.getDataInfo().getModified()).contains(CHANGED_AT);
        assertThat(group.getDataInfo().getModifiedByObjectId()).contains(7L);
        assertThat(group.getDomainEvents()).containsExactly(new GroupAttributeChanged(GROUP_ID,
                GroupHistoryAttribute.NAME, "Florbal Ostrava", "Florbal Poruba", 7L, DataInfoObjectType.USER,
                CHANGED_AT));
    }

    @Test
    void descriptionCanBeReplacedAndRemoved()
    {
        Group group = persisted();

        assertThat(group.changeDescription("Every Tuesday", CHANGED_AT, 7L, DataInfoObjectType.USER)).isTrue();
        assertThat(group.changeDescription(null, CHANGED_AT, 7L, DataInfoObjectType.USER)).isTrue();

        assertThat(group.getDescription()).isNull();
        assertThat(group.getDomainEvents()).extracting(GroupAttributeChanged::oldValue,
                        GroupAttributeChanged::newValue)
                .containsExactly(tuple("Weekly training", "Every Tuesday"), tuple("Every Tuesday", null));
    }

    @Test
    void visibilityChangeIsRecordedByEnumName()
    {
        Group group = persisted();

        assertThat(group.changeVisibility(GroupVisibility.PRIVATE, CHANGED_AT, 7L, DataInfoObjectType.USER))
                .isTrue();

        assertThat(group.getVisibility()).isEqualTo(GroupVisibility.PRIVATE);
        assertThat(group.getDomainEvents()).singleElement().satisfies(event ->
        {
            assertThat(event.attribute()).isEqualTo(GroupHistoryAttribute.VISIBILITY);
            assertThat(event.oldValue()).isEqualTo("PUBLIC");
            assertThat(event.newValue()).isEqualTo("PRIVATE");
        });
    }

    @Test
    void unchangedValuesDoNotModifyAuditNorRegisterEvents()
    {
        Group group = persisted();

        assertThat(group.changeName("Florbal Ostrava", CHANGED_AT, 7L, DataInfoObjectType.USER)).isFalse();
        assertThat(group.changeDescription("Weekly training", CHANGED_AT, 7L, DataInfoObjectType.USER)).isFalse();
        assertThat(group.changeVisibility(GroupVisibility.PUBLIC, CHANGED_AT, 7L, DataInfoObjectType.USER))
                .isFalse();

        assertThat(group.getDataInfo().getModified()).isEmpty();
        assertThat(group.getDomainEvents()).isEmpty();
    }

    @Test
    void rejectedChangeLeavesGroupUntouched()
    {
        Group group = persisted();

        assertThatThrownBy(() -> group.changeName("   ", CHANGED_AT, 7L, DataInfoObjectType.USER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> group.changeName("Renamed", CHANGED_AT, null, DataInfoObjectType.USER))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(group.getName()).isEqualTo("Florbal Ostrava");
        assertThat(group.getDataInfo().getModified()).isEmpty();
        assertThat(group.getDomainEvents()).isEmpty();
    }

    private static Group persisted()
    {
        Group group = Group.create("Florbal Ostrava", "Weekly training", GroupVisibility.PUBLIC, 7L, CREATED_AT);
        ReflectionTestUtils.setField(group, "id", GROUP_ID);

        return group;
    }
}
