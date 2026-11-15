package cz.osu.sportmeet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SportMeetApplicationTests
{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads()
    {
    }

    @Test
    void flywayInitialMigrationWasApplied()
    {
        Integer migrations = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success = TRUE", Integer.class
        );

        assertThat(migrations).isEqualTo(1);
    }
}
