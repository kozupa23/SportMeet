package cz.osu.sportmeet;

import cz.osu.sportmeet.user.persistence.entity.User;
import cz.osu.sportmeet.user.persistence.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Documents that identity lookups are exact and case-sensitive at the database level.
 * Case-insensitive identity matching is therefore a business rule enforced by
 * {@code UserValidator} normalization before persistence and lookup.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void loginLookupIsExactAndCaseSensitive()
    {
        persistUser("runner", "runner@example.com");

        assertThat(userRepository.existsByLogin("runner")).isTrue();
        assertThat(userRepository.findByLogin("runner")).isPresent();
        assertThat(userRepository.existsByLogin("RUNNER")).isFalse();
        assertThat(userRepository.findByLogin("RUNNER")).isEmpty();
        assertThat(userRepository.existsByLogin("someoneelse")).isFalse();
    }

    @Test
    void emailLookupIsExactAndCaseSensitive()
    {
        persistUser("runner", "runner@example.com");

        assertThat(userRepository.existsByEmail("runner@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("RUNNER@EXAMPLE.COM")).isFalse();
        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
    }

    private void persistUser(String login, String email)
    {
        User user = User.create(login, "Runner", email, "encoded-password",
                OffsetDateTime.parse("2026-11-10T00:00:00Z"));
        entityManager.persist(user);
        entityManager.flush();
    }
}
