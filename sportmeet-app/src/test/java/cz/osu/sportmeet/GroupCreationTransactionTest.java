package cz.osu.sportmeet;

import cz.osu.sportmeet.group.application.GroupFacade;
import cz.osu.sportmeet.group.application.dto.CreateGroupInput;
import cz.osu.sportmeet.group.persistence.entity.Membership;
import cz.osu.sportmeet.group.persistence.repository.MembershipRepository;
import cz.osu.sportmeet.user.persistence.entity.User;
import cz.osu.sportmeet.user.persistence.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * AC8: founding a group and creating the owner membership is one consistent operation.
 * The membership repository is replaced by a failing mock; the already inserted group must be rolled back.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@SpringBootTest
@ActiveProfiles("test")
class GroupCreationTransactionTest
{
    @Autowired
    private GroupFacade groupFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private MembershipRepository membershipRepository;

    @Test
    void failedOwnerMembershipRollsBackTheGroup()
    {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String login = "rollback" + suffix;
        String groupName = "Rollback " + suffix;
        userRepository.saveAndFlush(User.create(login, "Founder", login + "@example.com", "encoded",
                OffsetDateTime.now(ZoneOffset.UTC)));
        when(membershipRepository.save(any(Membership.class)))
                .thenThrow(new DataIntegrityViolationException("Simulated membership failure"));

        assertThatThrownBy(() -> groupFacade.createGroup(login, new CreateGroupInput(groupName, null, null)))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM T_GROUP WHERE NAME = ?", Integer.class,
                groupName)).isZero();
    }
}
