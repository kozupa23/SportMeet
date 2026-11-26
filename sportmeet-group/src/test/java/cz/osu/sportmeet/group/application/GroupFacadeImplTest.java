package cz.osu.sportmeet.group.application;

import cz.osu.sportmeet.common.application.InvalidInputException;
import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.group.application.dto.CreateGroupInput;
import cz.osu.sportmeet.group.application.dto.CreatedGroup;
import cz.osu.sportmeet.group.application.exception.InactiveUserException;
import cz.osu.sportmeet.group.application.validation.GroupValidator;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import cz.osu.sportmeet.group.domain.MembershipRole;
import cz.osu.sportmeet.group.domain.MembershipStatus;
import cz.osu.sportmeet.group.persistence.entity.Group;
import cz.osu.sportmeet.group.persistence.entity.Membership;
import cz.osu.sportmeet.group.persistence.repository.GroupRepository;
import cz.osu.sportmeet.group.persistence.repository.MembershipRepository;
import cz.osu.sportmeet.user.api.lookup.UserLookupFacade;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Verifies the group founding use case without Spring, web or database.
 * Atomicity of the two inserts is covered by {@code GroupCreationTransactionTest}.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
class GroupFacadeImplTest
{
    private static final long USER_ID = 7L;
    private static final long GROUP_ID = 3L;

    private final GroupRepository groups = mock(GroupRepository.class);
    private final MembershipRepository memberships = mock(MembershipRepository.class);
    private final UserLookupFacade userLookup = mock(UserLookupFacade.class);
    private final GroupFacadeImpl facade =
            new GroupFacadeImpl(groups, memberships, new GroupValidator(), userLookup);

    @BeforeEach
    void setUp()
    {
        when(userLookup.findActiveUserId("runner")).thenReturn(Optional.of(USER_ID));
        // Simulates the database assigning the IDENTITY key on insert.
        when(groups.save(any(Group.class))).thenAnswer(invocation ->
        {
            Group group = invocation.getArgument(0);
            ReflectionTestUtils.setField(group, "id", GROUP_ID);

            return group;
        });
        when(memberships.save(any(Membership.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void foundsNormalizedGroupWithFounderAsItsOnlyOwner()
    {
        CreatedGroup created = facade.createGroup("runner",
                new CreateGroupInput("  Florbal Ostrava ", "  Weekly training ", GroupVisibility.PUBLIC));

        assertThat(created).isEqualTo(new CreatedGroup(GROUP_ID, "Florbal Ostrava", "Weekly training",
                GroupVisibility.PUBLIC));

        ArgumentCaptor<Group> group = ArgumentCaptor.forClass(Group.class);
        ArgumentCaptor<Membership> membership = ArgumentCaptor.forClass(Membership.class);
        InOrder order = inOrder(groups, memberships);
        order.verify(groups).save(group.capture());
        order.verify(memberships).save(membership.capture());

        assertThat(group.getValue().isValid()).isTrue();
        assertThat(group.getValue().getDataInfo().getCreatedByObjectType()).isEqualTo(DataInfoObjectType.USER);
        assertThat(group.getValue().getDataInfo().getCreatedByObjectId()).contains(USER_ID);

        assertThat(membership.getValue().getGroupId()).isEqualTo(GROUP_ID);
        assertThat(membership.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(membership.getValue().getRole()).isEqualTo(MembershipRole.OWNER);
        assertThat(membership.getValue().getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(membership.getValue().getDataInfo().getCreated())
                .isEqualTo(group.getValue().getDataInfo().getCreated());
    }

    @Test
    void usesPrivateVisibilityWhenNoneRequested()
    {
        CreatedGroup created = facade.createGroup("runner", new CreateGroupInput("Volejbal", null, null));

        assertThat(created.visibility()).isEqualTo(GroupVisibility.PRIVATE);
        assertThat(created.description()).isNull();
    }

    @Test
    void inactiveUserCannotFoundGroup()
    {
        when(userLookup.findActiveUserId("blocked")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.createGroup("blocked", new CreateGroupInput("Volejbal", null, null)))
                .isInstanceOf(InactiveUserException.class);
        verifyNoInteractions(groups, memberships);
    }

    /**
     * An inactive user must not learn anything about the validity of the submitted data.
     */
    @Test
    void inactiveUserIsRejectedBeforeValidation()
    {
        when(userLookup.findActiveUserId("blocked")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.createGroup("blocked", new CreateGroupInput("   ", null, null)))
                .isInstanceOf(InactiveUserException.class);
    }

    @Test
    void invalidDataCreatesNothing()
    {
        assertThatThrownBy(() -> facade.createGroup("runner", new CreateGroupInput("   ", null, null)))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> facade.createGroup("runner",
                new CreateGroupInput("Volejbal", "x".repeat(GroupValidator.DESCRIPTION_MAX_LENGTH + 1), null)))
                .isInstanceOf(InvalidInputException.class);

        verify(userLookup, times(2)).findActiveUserId("runner");
        verifyNoInteractions(groups, memberships);
    }
}
