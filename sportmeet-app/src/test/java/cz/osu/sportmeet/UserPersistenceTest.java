package cz.osu.sportmeet;

import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.user.application.exception.DuplicateEmailException;
import cz.osu.sportmeet.user.application.exception.DuplicateLoginException;
import cz.osu.sportmeet.user.domain.UserState;
import cz.osu.sportmeet.user.persistence.UserConstraintViolations;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies the reference user entity against the H2 test schema managed by Flyway.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserPersistenceTest
{
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void translatesDatabaseLoginUniquenessViolation()
    {
        userRepository.saveAndFlush(User.create("constraintlogin", "First", "first@example.com",
                "encoded", OffsetDateTime.now()));

        DataIntegrityViolationException failure = assertThrows(DataIntegrityViolationException.class, () ->
                userRepository.saveAndFlush(User.create("constraintlogin", "Second", "second@example.com",
                        "encoded", OffsetDateTime.now())));

        assertThat(UserConstraintViolations.translate(failure)).isInstanceOf(DuplicateLoginException.class);
    }

    @Test
    void translatesDatabaseEmailUniquenessViolation()
    {
        userRepository.saveAndFlush(User.create("firstconstraint", "First", "constraint@example.com",
                "encoded", OffsetDateTime.now()));

        DataIntegrityViolationException failure = assertThrows(DataIntegrityViolationException.class, () ->
                userRepository.saveAndFlush(User.create("secondconstraint", "Second", "constraint@example.com",
                        "encoded", OffsetDateTime.now())));

        assertThat(UserConstraintViolations.translate(failure)).isInstanceOf(DuplicateEmailException.class);
    }

    /**
     * Verifies BIGINT identity generation, enum persistence, and embedded audit metadata.
     */
    @Test
    void persistsActiveUserWithAuditMetadata()
    {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-11-10T00:00:00Z");
        User user = User.create("sportfan", "Sport Fan", "sportfan@example.com", "encoded-test-password", createdAt);
        entityManager.persist(user);
        entityManager.flush();
        Long id = user.getId();
        entityManager.clear();

        User saved = entityManager.find(User.class, id);
        assertThat(id).isNotNull();
        assertThat(saved.getLogin()).isEqualTo("sportfan");
        assertThat(saved.getDisplayName()).isEqualTo("Sport Fan");
        assertThat(saved.isValid()).isTrue();
        assertThat(saved.getEmail()).isEqualTo("sportfan@example.com");
        assertThat(saved.getState()).isEqualTo(UserState.ACTIVE);
        assertThat(saved.getDataInfo().getCreated()).isEqualTo(createdAt);
        assertThat(saved.getDataInfo().getCreatedByObjectType()).isEqualTo(DataInfoObjectType.SYSTEM);
        assertThat(saved.getDataInfo().getCreatedByObjectId()).isEmpty();
        assertThat(saved.getDataInfo().getModified()).isEmpty();
        assertThat(saved.getDataInfo().getModifiedByObjectId()).isEmpty();
        assertThat(saved.getDataInfo().getModifiedByObjectType()).isEmpty();
        assertThat(saved.getVersion()).isNotNull();
    }

    /**
     * Verifies that the history table itself enforces the actor and password rules,
     * independently of the Java factory method.
     */
    @Test
    void historyTableEnforcesActorAndPasswordRules()
    {
        User user = User.create("historyfan", "History Fan", "historyfan@example.com", "encoded-test-password",
                OffsetDateTime.parse("2026-11-10T00:00:00Z"));
        entityManager.persist(user);
        entityManager.flush();
        Long id = user.getId();
        String insert = "INSERT INTO T_USER_HISTORY (USER_ID, CHANGED_BY, CHANGED_BY_TYPE, ATTRIBUTE_NAME, "
                + "OLD_VALUE, NEW_VALUE, CHANGED_AT) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        assertThat(jdbcTemplate.update(insert, id, null, "SYSTEM", "STATE", "ACTIVE", "BLOCKED")).isEqualTo(1);
        assertThat(jdbcTemplate.update(insert, id, id, "USER", "PASSWORD", null, null)).isEqualTo(1);

        assertThatThrownBy(() -> jdbcTemplate.update(insert, id, null, "USER", "EMAIL", "a@x.cz", "b@x.cz"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, id, id, "SYSTEM", "EMAIL", "a@x.cz", "b@x.cz"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, id, id, "USER", "PASSWORD", "old-hash", null))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(insert, id, id, "USER", "LOGIN", "a", "b"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
