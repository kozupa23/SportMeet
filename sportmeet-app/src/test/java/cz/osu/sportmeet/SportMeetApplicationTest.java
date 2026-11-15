package cz.osu.sportmeet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SportMeetApplicationTest
{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads()
    {
    }

    /**
     * Verifies that all Flyway migrations on the classpath were discovered and applied
     * successfully, without depending on specific migration version numbers.
     */
    @Test
    void flywayMigrationsWereAppliedSuccessfully()
    {
        Integer failedMigrations = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = FALSE", Integer.class
        );
        assertThat(failedMigrations).isZero();

        Integer appliedMigrations = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE", Integer.class
        );
        assertThat(appliedMigrations).isGreaterThanOrEqualTo(2);
    }
}
