package cz.osu.sportmeet;

import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import cz.osu.sportmeet.group.domain.MembershipRole;
import cz.osu.sportmeet.group.domain.MembershipStatus;
import cz.osu.sportmeet.group.persistence.entity.Group;
import cz.osu.sportmeet.group.persistence.entity.Membership;
import cz.osu.sportmeet.group.persistence.repository.GroupRepository;
import cz.osu.sportmeet.group.persistence.repository.MembershipRepository;
import cz.osu.sportmeet.user.persistence.entity.User;
import cz.osu.sportmeet.user.persistence.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the group and membership entities against the H2 test schema managed by Flyway.
 * The partial unique indexes (one active owner, one active membership) exist only in PostgreSQL
 * and therefore cannot be verified here.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GroupPersistenceTest
{
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-11-20T18:00:00Z");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    /**
     * Verifies identity generation, enum mapping, embedded audit metadata and repository queries.
     */
    @Test
    void persistsGroupWithOwnerMembership()
    {
        Long founderId = founder("groupfounder");
        Long groupId = groupRepository.saveAndFlush(Group.create("Florbal Ostrava", "Weekly training",
                GroupVisibility.PUBLIC, founderId, CREATED_AT)).getId();
        membershipRepository.saveAndFlush(Membership.owner(groupId, founderId, CREATED_AT));
        entityManager.clear();

        Group saved = groupRepository.findById(groupId).orElseThrow();
        assertThat(saved.getName()).isEqualTo("Florbal Ostrava");
        assertThat(saved.getDescription()).isEqualTo("Weekly training");
        assertThat(saved.getVisibility()).isEqualTo(GroupVisibility.PUBLIC);
        assertThat(saved.isValid()).isTrue();
        assertThat(saved.getDataInfo().getCreated()).isEqualTo(CREATED_AT);
        assertThat(saved.getDataInfo().getCreatedByObjectType()).isEqualTo(DataInfoObjectType.USER);
        assertThat(saved.getDataInfo().getCreatedByObjectId()).contains(founderId);
        assertThat(saved.getVersion()).isNotNull();

        assertThat(membershipRepository.findByGroupId(groupId)).singleElement().satisfies(membership ->
        {
            assertThat(membership.getUserId()).isEqualTo(founderId);
            assertThat(membership.getRole()).isEqualTo(MembershipRole.OWNER);
            assertThat(membership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
            assertThat(membership.getEndedAt()).isNull();
            assertThat(membership.isValid()).isTrue();
            assertThat(membership.getVersion()).isNotNull();
        });

        // JPQL over the entity named "Group" must work although GROUP is an SQL keyword.
        assertThat(groupRepository.count()).isPositive();
    }

    /**
     * Verifies that the group table itself enforces its rules, independently of the Java factory method.
     */
    @Test
    void groupTableEnforcesNameVisibilityAndActorRules()
    {
        Long founderId = founder("groupchecks");
        String insert = "INSERT INTO T_GROUP (NAME, VISIBILITY, CREATED_AT, CREATED_BY_OBJECT_ID, "
                + "CREATED_BY_OBJECT_TYPE, VERSION) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, 0)";

        assertThat(jdbcTemplate.update(insert, "Valid group", "PRIVATE", founderId, "USER")).isEqualTo(1);

        assertThatThrownBy(() -> jdbcTemplate.update(insert, "   ", "PUBLIC", founderId, "USER"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, "Secret group", "SECRET", founderId, "USER"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, "No actor", "PUBLIC", null, "USER"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, "System with ID", "PUBLIC", founderId, "SYSTEM"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * Verifies that the membership table enforces allowed roles and statuses and a consistent end time.
     */
    @Test
    void membershipTableEnforcesRoleStatusAndEndRules()
    {
        Long founderId = founder("membershipchecks");
        Long groupId = groupRepository.saveAndFlush(Group.create("Volejbal", null,
                GroupVisibility.PRIVATE, founderId, CREATED_AT)).getId();
        String insert = "INSERT INTO T_GROUP_MEMBERSHIP (GROUP_ID, USER_ID, ROLE, STATUS, ENDED_AT, CREATED_AT, "
                + "CREATED_BY_OBJECT_ID, CREATED_BY_OBJECT_TYPE, VERSION) VALUES (?, ?, ?, ?, ?, ?, ?, 'USER', 0)";
        OffsetDateTime later = CREATED_AT.plusDays(1);
        OffsetDateTime earlier = CREATED_AT.minusDays(1);

        assertThat(jdbcTemplate.update(insert, groupId, founderId, "MEMBER", "ENDED", later, CREATED_AT, founderId))
                .isEqualTo(1);

        assertThatThrownBy(() -> jdbcTemplate.update(insert, groupId, founderId, "ADMIN", "ACTIVE", null,
                CREATED_AT, founderId)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, groupId, founderId, "MEMBER", "PENDING", null,
                CREATED_AT, founderId)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, groupId, founderId, "MEMBER", "ACTIVE", later,
                CREATED_AT, founderId)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, groupId, founderId, "MEMBER", "ENDED", null,
                CREATED_AT, founderId)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, groupId, founderId, "MEMBER", "ENDED", earlier,
                CREATED_AT, founderId)).isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * Authorization of the group detail relies on what a "current" membership is: ACTIVE and technically valid.
     * Verified against the database, because the repository methods are derived queries.
     */
    @Test
    void currentMembershipMeansActiveAndValid()
    {
        Long founderId = founder("currentfounder");
        Long activeId = founder("currentactive");
        Long endedId = founder("currentended");
        Long invalidId = founder("currentinvalid");
        Long groupId = groupRepository.saveAndFlush(Group.create("Tenis", null,
                GroupVisibility.PRIVATE, founderId, CREATED_AT)).getId();
        membershipRepository.saveAndFlush(Membership.owner(groupId, founderId, CREATED_AT));
        String insert = "INSERT INTO T_GROUP_MEMBERSHIP (GROUP_ID, USER_ID, ROLE, STATUS, ENDED_AT, VALID, "
                + "CREATED_AT, CREATED_BY_OBJECT_ID, CREATED_BY_OBJECT_TYPE, VERSION) "
                + "VALUES (?, ?, 'MEMBER', ?, ?, ?, ?, ?, 'USER', 0)";
        jdbcTemplate.update(insert, groupId, activeId, "ACTIVE", null, true, CREATED_AT, activeId);
        jdbcTemplate.update(insert, groupId, endedId, "ENDED", CREATED_AT.plusDays(1), true, CREATED_AT, endedId);
        jdbcTemplate.update(insert, groupId, invalidId, "ACTIVE", null, false, CREATED_AT, invalidId);

        assertThat(membershipRepository.findCurrentMembers(groupId)).extracting(Membership::getUserId)
                .containsExactlyInAnyOrder(founderId, activeId);
        assertThat(membershipRepository.findCurrentMembership(groupId, activeId)).isPresent();
        assertThat(membershipRepository.findCurrentMembership(groupId, endedId)).isEmpty();
        assertThat(membershipRepository.findCurrentMembership(groupId, invalidId)).isEmpty();

        assertThat(groupRepository.findByIdAndValidTrue(groupId)).isPresent();
        jdbcTemplate.update("UPDATE T_GROUP SET VALID = FALSE WHERE ID = ?", groupId);
        assertThat(groupRepository.findByIdAndValidTrue(groupId)).isEmpty();
    }

    /**
     * Verifies that the history table itself enforces its rules: a USER actor has an identifier, a SYSTEM actor
     * has none, only known attributes are stored, and exactly membership changes identify the affected member.
     */
    @Test
    void historyTableEnforcesActorAttributeAndMemberRules()
    {
        Long founderId = founder("historychecks");
        Long groupId = groupRepository.saveAndFlush(Group.create("Šachy", null,
                GroupVisibility.PRIVATE, founderId, CREATED_AT)).getId();
        String insert = "INSERT INTO T_GROUP_HISTORY (GROUP_ID, MEMBER_USER_ID, ATTRIBUTE_NAME, OLD_VALUE, NEW_VALUE, "
                + "CHANGED_BY, CHANGED_BY_TYPE, CHANGED_AT) VALUES (?, ?, ?, 'old', 'new', ?, ?, CURRENT_TIMESTAMP)";

        assertThat(jdbcTemplate.update(insert, groupId, null, "NAME", founderId, "USER")).isEqualTo(1);
        assertThat(jdbcTemplate.update(insert, groupId, null, "VALID", null, "SYSTEM")).isEqualTo(1);
        assertThat(jdbcTemplate.update(insert, groupId, founderId, "MEMBERSHIP_ROLE", founderId, "USER"))
                .isEqualTo(1);

        assertThatThrownBy(() -> jdbcTemplate.update(insert, groupId, null, "NAME", null, "USER"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, groupId, null, "NAME", founderId, "SYSTEM"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, groupId, null, "OWNER_ID", founderId, "USER"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, groupId, null, "MEMBERSHIP_STATUS", founderId, "USER"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, groupId, founderId, "NAME", founderId, "USER"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * Persists a user that can be referenced by groups and memberships.
     */
    private Long founder(String login)
    {
        return userRepository.saveAndFlush(User.create(login, "Founder", login + "@example.com",
                "encoded-test-password", CREATED_AT)).getId();
    }
}
