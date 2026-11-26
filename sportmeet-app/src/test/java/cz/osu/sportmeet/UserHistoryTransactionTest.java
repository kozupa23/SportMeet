package cz.osu.sportmeet;

import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.user.persistence.entity.User;
import cz.osu.sportmeet.user.persistence.repository.UserHistoryRepository;
import cz.osu.sportmeet.user.persistence.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies atomic rollback when writing user change history fails.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@SpringBootTest
@ActiveProfiles("test")
class UserHistoryTransactionTest
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserHistoryRepository userHistoryRepository;

    @Autowired
    private TransactionTemplate transaction;

    @Test
    void historyFailureRollsBackAlreadyFlushedUserChange()
    {
        String login = "rollback" + UUID.randomUUID().toString().replace("-", "");
        Long userId = transaction.execute(status -> userRepository.saveAndFlush(
                User.create(login, "Original", login + "@example.com", "encoded",
                        OffsetDateTime.now(ZoneOffset.UTC))).getId());

        assertThatThrownBy(() -> transaction.executeWithoutResult(status ->
        {
            User user = userRepository.findByLogin(login).orElseThrow();
            user.changeDisplayName("Changed", OffsetDateTime.now(ZoneOffset.UTC), Long.MAX_VALUE,
                    DataInfoObjectType.USER);
            userRepository.saveAndFlush(user);
        })).isInstanceOf(DataIntegrityViolationException.class);

        transaction.executeWithoutResult(status ->
        {
            assertThat(userRepository.findByLogin(login).orElseThrow().getDisplayName()).isEqualTo("Original");
            assertThat(userHistoryRepository.findByUserId(userId, org.springframework.data.domain.PageRequest.of(0, 10)))
                    .isEmpty();
        });
    }
}
