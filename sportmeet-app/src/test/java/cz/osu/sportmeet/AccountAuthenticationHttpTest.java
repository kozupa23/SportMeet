package cz.osu.sportmeet;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
class AccountAuthenticationHttpTest
{
    @LocalServerPort
    private int port;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldRequireCsrfAndInvalidateSessionAfterLogout() throws Exception
    {
        try (HttpClient client = newClient())
        {
            String login = "user" + UUID.randomUUID().toString().replace("-", "");
            String password = "SafePassword-123!";
            register(client, login, password);

            assertThat(client.send(get("/api/v1/auth/session"), HttpResponse.BodyHandlers.ofString()).statusCode())
                    .isEqualTo(401);

            String form = "login=" + encode(login) + "&password=" + encode(password);
            HttpRequest noCsrf = HttpRequest.newBuilder(uri("/api/v1/auth/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            assertThat(client.send(noCsrf, HttpResponse.BodyHandlers.ofString()).statusCode())
                    .isEqualTo(403);

            JsonNode csrf = csrf(client);
            assertThat(client.send(postForm(form, csrf), HttpResponse.BodyHandlers.ofString()).statusCode())
                    .isEqualTo(204);

            HttpResponse<String> authenticated = client.send(get("/api/v1/auth/session"),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(authenticated.statusCode()).isEqualTo(200);
            assertThat(authenticated.body()).contains("\"login\":\"" + login + "\"");
            assertThat(authenticated.body()).doesNotContain(password, "passwordHash");

            assertThat(sessionCount(login)).isEqualTo(1);
            assertThat(securityContextCount(login)).isEqualTo(1);

            csrf = csrf(client);
            HttpRequest logout = HttpRequest.newBuilder(uri("/api/v1/auth/logout"))
                    .header(csrf.get("headerName").asString(), csrf.get("token").asString())
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            assertThat(client.send(logout, HttpResponse.BodyHandlers.ofString()).statusCode())
                    .isEqualTo(204);

            assertThat(sessionCount(login)).isZero();

            assertThat(client.send(get("/api/v1/auth/session"), HttpResponse.BodyHandlers.ofString()).statusCode())
                    .isEqualTo(401);
        }
    }

    @Test
    void shouldRejectIncorrectPassword() throws Exception
    {
        try (HttpClient client = newClient())
        {
            String login = "user" + UUID.randomUUID().toString().replace("-", "");
            register(client, login, "SafePassword-123!");

            JsonNode csrf = csrf(client);
            String form = "login=" + encode(login) + "&password=" + encode("incorrect-password");
            assertThat(client.send(postForm(form, csrf), HttpResponse.BodyHandlers.ofString()).statusCode())
                    .isEqualTo(401);
            assertThat(client.send(get("/api/v1/auth/session"), HttpResponse.BodyHandlers.ofString()).statusCode())
                    .isEqualTo(401);
        }
    }

    private long sessionCount(String login)
    {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM SPRING_SESSION WHERE PRINCIPAL_NAME = ?", Long.class, login);

        return java.util.Objects.requireNonNull(count);
    }

    private long securityContextCount(String login)
    {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM SPRING_SESSION_ATTRIBUTES a
                JOIN SPRING_SESSION s ON a.SESSION_PRIMARY_ID = s.PRIMARY_ID
                WHERE s.PRINCIPAL_NAME = ?
                  AND a.ATTRIBUTE_NAME = 'SPRING_SECURITY_CONTEXT'
                """, Long.class, login);

        return java.util.Objects.requireNonNull(count);
    }

    private void register(HttpClient client, String login, String password) throws Exception
    {
        String json = jsonMapper.writeValueAsString(new RegistrationRequest(
                login, "Sport Fan", login + "@example.com", password));
        HttpRequest request = HttpRequest.newBuilder(uri("/api/v1/accounts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        assertThat(client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(201);
    }

    private JsonNode csrf(HttpClient client) throws Exception
    {
        HttpResponse<String> response = client.send(get("/api/v1/auth/csrf"),
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);

        return jsonMapper.readTree(response.body());
    }

    private HttpRequest postForm(String form, JsonNode csrf)
    {
        return HttpRequest.newBuilder(uri("/api/v1/auth/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header(csrf.get("headerName").asString(), csrf.get("token").asString())
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
    }

    private HttpRequest get(String path)
    {
        return HttpRequest.newBuilder(uri(path)).GET().build();
    }

    private URI uri(String path)
    {
        return URI.create("http://localhost:" + port + path);
    }

    private static HttpClient newClient()
    {
        return HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .build();
    }

    private static String encode(String value)
    {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record RegistrationRequest(String login, String displayName, String email, String password)
    {
    }
}
