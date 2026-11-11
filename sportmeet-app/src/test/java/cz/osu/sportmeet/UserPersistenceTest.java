package cz.osu.sportmeet;

import cz.osu.sportmeet.account.domain.UserState;
import cz.osu.sportmeet.account.persistence.entity.User;
import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the reference account entity against the H2 test schema managed by Flyway.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserPersistenceTest
{
    @Autowired
    private EntityManager entityManager;

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
}
