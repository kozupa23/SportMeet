package cz.osu.sportmeet;

import cz.osu.sportmeet.support.HttpTestSupport;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises authentication, CSRF and session lifecycle over real HTTP.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserAuthenticationHttpTest
{
    private static final String SESSION = "/api/v1/auth/session";

    @LocalServerPort
    private int port;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HttpTestSupport http;

    @BeforeEach
    void setUp()
    {
        http = new HttpTestSupport(port, jsonMapper);
    }

    @Test
    void shouldRequireCsrfAndInvalidateSessionAfterLogout() throws Exception
    {
        try (HttpClient client = HttpTestSupport.newClient())
        {
            String login = HttpTestSupport.uniqueLogin("user");
            String password = HttpTestSupport.DEFAULT_PASSWORD;
            http.register(client, login, password);

            assertThat(http.get(client, SESSION).statusCode()).isEqualTo(401);
            assertThat(http.loginWithoutCsrf(client, login, password).statusCode()).isEqualTo(403);

            // The submitted login is normalized by the user module.
            assertThat(http.login(client, " " + login.toUpperCase() + " ", password).statusCode())
                    .isEqualTo(204);

            HttpResponse<String> authenticated = http.get(client, SESSION);
            assertThat(authenticated.statusCode()).isEqualTo(200);
            assertThat(authenticated.body()).contains("\"login\":\"" + login + "\"");
            assertThat(authenticated.body()).doesNotContain(password, "passwordHash");

            assertThat(sessionCount(login)).isEqualTo(1);
            assertThat(securityContextCount(login)).isEqualTo(1);

            assertThat(http.logout(client).statusCode()).isEqualTo(204);

            assertThat(sessionCount(login)).isZero();
            assertThat(http.get(client, SESSION).statusCode()).isEqualTo(401);
        }
    }

    @Test
    void shouldRejectIncorrectPassword() throws Exception
    {
        try (HttpClient client = HttpTestSupport.newClient())
        {
            String login = HttpTestSupport.uniqueLogin("user");
            http.register(client, login, HttpTestSupport.DEFAULT_PASSWORD);

            assertThat(http.login(client, login, "incorrect-password").statusCode()).isEqualTo(401);
            assertThat(http.get(client, SESSION).statusCode()).isEqualTo(401);
        }
    }

    private long sessionCount(String login)
    {
        return Objects.requireNonNull(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM SPRING_SESSION WHERE PRINCIPAL_NAME = ?", Long.class, login));
    }

    private long securityContextCount(String login)
    {
        return Objects.requireNonNull(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM SPRING_SESSION_ATTRIBUTES a
                JOIN SPRING_SESSION s ON a.SESSION_PRIMARY_ID = s.PRIMARY_ID
                WHERE s.PRINCIPAL_NAME = ?
                  AND a.ATTRIBUTE_NAME = 'SPRING_SECURITY_CONTEXT'
                """, Long.class, login));
    }
}
