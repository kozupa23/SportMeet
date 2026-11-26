package cz.osu.sportmeet.group.application;

import cz.osu.sportmeet.common.application.InvalidInputException;
import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.group.application.dto.CreateGroupInput;
import cz.osu.sportmeet.group.application.dto.CreatedGroup;
import cz.osu.sportmeet.group.application.dto.GroupDetail;
import cz.osu.sportmeet.group.application.dto.GroupMember;
import cz.osu.sportmeet.group.application.exception.GroupNotFoundException;
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
import cz.osu.sportmeet.user.api.lookup.UserSummary;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Verifies the group use cases without Spring, web or database.
 * Atomicity of founding is covered by {@code GroupCreationTransactionTest}.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
class GroupFacadeImplTest
{
    private static final long USER_ID = 7L;
    private static final long GROUP_ID = 3L;
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-11-20T18:00:00Z");

    private final GroupRepository groups = mock(GroupRepository.class);
    private final MembershipRepository memberships = mock(MembershipRepository.class);
    private final UserLookupFacade userLookup = mock(UserLookupFacade.class);
    private final GroupFacadeImpl facade = new GroupFacadeImpl(groups, memberships, new GroupValidator(),
            new GroupAccess(userLookup, memberships), userLookup);

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

    // ---------------------------------------------------------------- founding a group (US-04-01)

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
     * The facade checks the account before its own business validation.
     * (Bean Validation of the HTTP request runs even earlier, in the controller.)
     */
    @Test
    void inactiveUserIsRejectedBeforeBusinessValidation()
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

    // ---------------------------------------------------------------- group detail (US-04-02)

    /**
     * AC3–AC6: current members are listed with their roles, owner first, names in Czech order
     * (in Czech, "Ch" sorts after "H").
     */
    @Test
    void memberSeesGroupWithCurrentMembersOrderedByRoleAndName()
    {
        validGroup(GroupVisibility.PRIVATE);
        when(memberships.findCurrentMembership(GROUP_ID, USER_ID))
                .thenReturn(Optional.of(membership(USER_ID, MembershipRole.MEMBER)));
        when(memberships.findCurrentMembers(GROUP_ID)).thenReturn(List.of(
                membership(USER_ID, MembershipRole.MEMBER),
                membership(20L, MembershipRole.MEMBER),
                membership(30L, MembershipRole.OWNER),
                membership(40L, MembershipRole.SECONDARY_OWNER),
                membership(50L, MembershipRole.MEMBER)));
        when(userLookup.findUserSummaries(anyCollection())).thenReturn(List.of(
                new UserSummary(USER_ID, "Ivan"),
                new UserSummary(20L, "Chrudoš"),
                new UserSummary(30L, "Zdeněk"),
                new UserSummary(40L, "Anna"),
                new UserSummary(50L, "Hana")));

        GroupDetail detail = facade.getGroup("runner", GROUP_ID);

        assertThat(detail.name()).isEqualTo("Florbal Ostrava");
        assertThat(detail.description()).isEqualTo("Weekly training");
        assertThat(detail.myRole()).isEqualTo(MembershipRole.MEMBER);
        assertThat(detail.members()).extracting(GroupMember::displayName)
                .containsExactly("Zdeněk", "Anna", "Hana", "Chrudoš", "Ivan");
        assertThat(detail.members()).extracting(GroupMember::role).containsExactly(
                MembershipRole.OWNER, MembershipRole.SECONDARY_OWNER,
                MembershipRole.MEMBER, MembershipRole.MEMBER, MembershipRole.MEMBER);

        // All members' names come from a single lookup, not one lookup per member.
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Long>> ids = ArgumentCaptor.forClass(Collection.class);
        verify(userLookup, times(1)).findUserSummaries(ids.capture());
        assertThat(ids.getValue()).containsExactlyInAnyOrder(USER_ID, 20L, 30L, 40L, 50L);
    }

    /**
     * AC3, AC8: membership, not visibility, decides whether members are shown; a member of a public group
     * sees them too.
     */
    @Test
    void memberOfPublicGroupAlsoSeesMembers()
    {
        validGroup(GroupVisibility.PUBLIC);
        when(memberships.findCurrentMembership(GROUP_ID, USER_ID))
                .thenReturn(Optional.of(membership(USER_ID, MembershipRole.MEMBER)));
        when(memberships.findCurrentMembers(GROUP_ID)).thenReturn(List.of(
                membership(USER_ID, MembershipRole.MEMBER), membership(30L, MembershipRole.OWNER)));
        when(userLookup.findUserSummaries(anyCollection())).thenReturn(List.of(
                new UserSummary(USER_ID, "Ivan"), new UserSummary(30L, "Zdeněk")));

        GroupDetail detail = facade.getGroup("runner", GROUP_ID);

        assertThat(detail.myRole()).isEqualTo(MembershipRole.MEMBER);
        assertThat(detail.members()).extracting(GroupMember::displayName).containsExactly("Zdeněk", "Ivan");
    }

    /**
     * AC1, AC2, AC8: a non-member sees only basic information of a public group, never its members.
     */
    @Test
    void nonMemberSeesOnlyBasicInformationOfPublicGroup()
    {
        validGroup(GroupVisibility.PUBLIC);
        when(memberships.findCurrentMembership(GROUP_ID, USER_ID)).thenReturn(Optional.empty());

        GroupDetail detail = facade.getGroup("runner", GROUP_ID);

        assertThat(detail.visibility()).isEqualTo(GroupVisibility.PUBLIC);
        assertThat(detail.myRole()).isNull();
        assertThat(detail.members()).isNull();
        verify(memberships, never()).findCurrentMembers(any());
        verify(userLookup, never()).findUserSummaries(anyCollection());
    }

    /**
     * AC8, AC10: a private group looks exactly like a missing one to a non-member.
     */
    @Test
    void nonMemberOfPrivateGroupGetsSameResultAsForMissingGroup()
    {
        validGroup(GroupVisibility.PRIVATE);
        when(memberships.findCurrentMembership(GROUP_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.getGroup("runner", GROUP_ID)).isInstanceOf(GroupNotFoundException.class);
        verify(memberships, never()).findCurrentMembers(any());
    }

    /**
     * AC9, AC10: missing and invalidated groups (the repository returns only valid ones) are not found,
     * not even for their members.
     */
    @Test
    void missingOrInvalidatedGroupIsNotFound()
    {
        when(groups.findByIdAndValidTrue(GROUP_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.getGroup("runner", GROUP_ID)).isInstanceOf(GroupNotFoundException.class);
        verifyNoInteractions(memberships);
    }

    @Test
    void inactiveUserCannotViewGroup()
    {
        when(userLookup.findActiveUserId("blocked")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.getGroup("blocked", GROUP_ID)).isInstanceOf(InactiveUserException.class);
        verifyNoInteractions(groups, memberships);
    }

    private void validGroup(GroupVisibility visibility)
    {
        Group group = Group.create("Florbal Ostrava", "Weekly training", visibility, 30L, CREATED_AT);
        ReflectionTestUtils.setField(group, "id", GROUP_ID);
        when(groups.findByIdAndValidTrue(GROUP_ID)).thenReturn(Optional.of(group));
    }

    /**
     * Builds a current membership with any role; only the owner factory exists in production code so far.
     */
    private static Membership membership(long userId, MembershipRole role)
    {
        Membership membership = Membership.owner(GROUP_ID, userId, CREATED_AT);
        ReflectionTestUtils.setField(membership, "role", role);

        return membership;
    }
}
