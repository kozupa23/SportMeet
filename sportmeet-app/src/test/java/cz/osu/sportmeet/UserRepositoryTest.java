package cz.osu.sportmeet;

import cz.osu.sportmeet.account.persistence.entity.User;
import cz.osu.sportmeet.account.persistence.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies repository uniqueness lookups in isolation from the web layer.
 * Case-insensitive identity matching is a business rule enforced by
 * {@code UserValidator} normalization before persistence, not by the database
 * column itself; these tests document and protect that assumption.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Verifies that an existing login is reported as present and an unrelated one is not.
     */
    @Test
    void existsByLoginReturnsTrueOnlyForStoredValue()
    {
        persistUser("runner", "runner@example.com");

        assertThat(userRepository.existsByLogin("runner")).isTrue();
        assertThat(userRepository.existsByLogin("someoneelse")).isFalse();
    }

    /**
     * Verifies that an existing email is reported as present and an unrelated one is not.
     */
    @Test
    void existsByEmailReturnsTrueOnlyForStoredValue()
    {
        persistUser("runner", "runner@example.com");

        assertThat(userRepository.existsByEmail("runner@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
    }

    /**
     * Documents that login lookups are case-sensitive at the database level, so
     * callers must normalize identity values before checking or persisting them.
     */
    @Test
    void existsByLoginIsCaseSensitiveAtDatabaseLevel()
    {
        persistUser("runner", "runner@example.com");

        assertThat(userRepository.existsByLogin("RUNNER")).isFalse();
    }

    /**
     * Documents that email lookups are case-sensitive at the database level, so
     * callers must normalize identity values before checking or persisting them.
     */
    @Test
    void existsByEmailIsCaseSensitiveAtDatabaseLevel()
    {
        persistUser("runner", "runner@example.com");

        assertThat(userRepository.existsByEmail("RUNNER@EXAMPLE.COM")).isFalse();
    }

    private void persistUser(String login, String email)
    {
        User user = User.create(login, "Runner", email, "encoded-password",
                OffsetDateTime.parse("2026-11-10T00:00:00Z"));
        entityManager.persist(user);
        entityManager.flush();
    }
}
