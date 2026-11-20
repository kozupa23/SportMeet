package cz.osu.sportmeet;

import cz.osu.sportmeet.support.HttpTestSupport;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HTTP integration coverage for the authenticated profile, its changes and their history.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserDetailHttpTest
{
    private static final String ME = "/api/v1/users/me";
    private static final String HISTORY = ME + "/history";

    @LocalServerPort
    private int port;

    @Autowired
    private JsonMapper mapper;

    private HttpTestSupport http;

    @BeforeEach
    void setUp()
    {
        http = new HttpTestSupport(port, mapper);
    }

    @Test
    void profileRequiresAuthenticationAndCsrf() throws Exception
    {
        try (HttpClient client = HttpTestSupport.newClient())
        {
            String login = HttpTestSupport.uniqueLogin("prof");
            http.register(client, login, HttpTestSupport.DEFAULT_PASSWORD);

            assertThat(http.get(client, ME).statusCode()).isEqualTo(401);
            assertThat(http.get(client, HISTORY).statusCode()).isEqualTo(401);

            assertThat(http.login(client, login, HttpTestSupport.DEFAULT_PASSWORD).statusCode()).isEqualTo(204);
            assertThat(http.send(client, "PATCH", ME, "{\"displayName\":\"New Name\"}", null).statusCode())
                    .isEqualTo(403);
        }
    }

    @Test
    void displayNameChangeIsRecordedExactlyOnce() throws Exception
    {
        try (HttpClient client = HttpTestSupport.newClient())
        {
            String login = HttpTestSupport.uniqueLogin("prof");
            http.registerAndLogin(client, login);

            JsonNode me = http.json(http.get(client, ME));
            assertThat(me.get("login").asString()).isEqualTo(login);
            assertThat(me.get("displayName").asString()).isEqualTo("Sport Fan");
            assertThat(me.get("updatedAt").isNull()).isTrue();
            assertThat(me.has("passwordHash")).isFalse();

            HttpResponse<String> change = http.patch(client, ME, "{\"displayName\":\"  New Name  \"}");
            assertThat(change.statusCode()).isEqualTo(200);
            JsonNode result = http.json(change);
            assertThat(result.get("displayName").asString()).isEqualTo("New Name");
            assertThat(result.get("updatedAt").isNull()).isFalse();

            // The same value again is a no-op and must not create another history entry.
            assertThat(http.patch(client, ME, "{\"displayName\":\"New Name\"}").statusCode()).isEqualTo(200);

            JsonNode history = http.json(http.get(client, HISTORY));
            assertThat(history.get("totalElements").asInt()).isEqualTo(1);
            JsonNode entry = history.get("content").get(0);
            assertThat(entry.get("attributeName").asString()).isEqualTo("DISPLAY_NAME");
            assertThat(entry.get("oldValue").asString()).isEqualTo("Sport Fan");
            assertThat(entry.get("newValue").asString()).isEqualTo("New Name");
            assertThat(entry.get("changedBy").asLong()).isEqualTo(me.get("id").asLong());
            assertThat(entry.get("changedByType").asString()).isEqualTo("USER");
        }
    }

    @Test
    void emailChangeIsNormalizedAndRecorded() throws Exception
    {
        try (HttpClient client = HttpTestSupport.newClient())
        {
            String login = HttpTestSupport.uniqueLogin("mail");
            http.registerAndLogin(client, login);

            HttpResponse<String> change = http.patch(client, ME,
                    "{\"email\":\"  " + login.toUpperCase() + "@New.Example.COM \"}");
            assertThat(change.statusCode()).isEqualTo(200);
            assertThat(http.json(change).get("email").asString()).isEqualTo(login + "@new.example.com");

            JsonNode entry = http.json(http.get(client, HISTORY)).get("content").get(0);
            assertThat(entry.get("attributeName").asString()).isEqualTo("EMAIL");
            assertThat(entry.get("oldValue").asString()).isEqualTo(login + "@example.com");
            assertThat(entry.get("newValue").asString()).isEqualTo(login + "@new.example.com");
        }
    }

    @Test
    void changingBothAttributesRecordsBothHistoryEntries() throws Exception
    {
        try (HttpClient client = HttpTestSupport.newClient())
        {
            String login = HttpTestSupport.uniqueLogin("both");
            http.registerAndLogin(client, login);

            assertThat(http.patch(client, ME,
                            "{\"displayName\":\"New Name\",\"email\":\"new-" + login + "@example.com\"}")
                    .statusCode()).isEqualTo(200);
            JsonNode history = http.json(http.get(client, HISTORY));
            assertThat(history.get("totalElements").asInt()).isEqualTo(2);
            assertThat(history.get("content").get(0).get("attributeName").asString()).isEqualTo("EMAIL");
            assertThat(history.get("content").get(1).get("attributeName").asString()).isEqualTo("DISPLAY_NAME");
        }
    }

    @Test
    void emailOfAnotherUserIsRejectedAtomically() throws Exception
    {
        try (HttpClient other = HttpTestSupport.newClient(); HttpClient client = HttpTestSupport.newClient())
        {
            String otherLogin = HttpTestSupport.uniqueLogin("taken");
            http.register(other, otherLogin, HttpTestSupport.DEFAULT_PASSWORD);
            String login = HttpTestSupport.uniqueLogin("prof");
            http.registerAndLogin(client, login);

            HttpResponse<String> conflict = http.patch(client, ME,
                    "{\"displayName\":\"Changed\",\"email\":\"" + otherLogin.toUpperCase() + "@example.com\"}");
            assertThat(conflict.statusCode()).isEqualTo(409);
            assertThat(conflict.headers().firstValue("Content-Type")).hasValueSatisfying(
                    value -> assertThat(value).startsWith("application/problem+json"));

            // Neither attribute changed and no history was written.
            JsonNode me = http.json(http.get(client, ME));
            assertThat(me.get("displayName").asString()).isEqualTo("Sport Fan");
            assertThat(me.get("email").asString()).isEqualTo(login + "@example.com");
            assertThat(http.json(http.get(client, HISTORY)).get("totalElements").asInt()).isZero();
        }
    }

    @Test
    void historyIsPagedNewestFirst() throws Exception
    {
        try (HttpClient client = HttpTestSupport.newClient())
        {
            http.registerAndLogin(client, HttpTestSupport.uniqueLogin("page"));
            for (String name : new String[]{"First", "Second", "Third"})
            {
                assertThat(http.patch(client, ME, "{\"displayName\":\"" + name + "\"}").statusCode()).isEqualTo(200);
            }

            JsonNode first = http.json(http.get(client, HISTORY + "?page=0&size=2"));
            assertThat(first.get("totalElements").asInt()).isEqualTo(3);
            assertThat(first.get("totalPages").asInt()).isEqualTo(2);
            assertThat(first.get("page").asInt()).isZero();
            assertThat(first.get("size").asInt()).isEqualTo(2);
            assertThat(first.get("content")).hasSize(2);
            assertThat(first.get("content").get(0).get("newValue").asString()).isEqualTo("Third");
            assertThat(first.get("content").get(1).get("newValue").asString()).isEqualTo("Second");

            JsonNode second = http.json(http.get(client, HISTORY + "?page=1&size=2"));
            assertThat(second.get("content")).hasSize(1);
            assertThat(second.get("content").get(0).get("newValue").asString()).isEqualTo("First");
        }
    }

    @Test
    void invalidRequestsAreRejectedWithoutChanges() throws Exception
    {
        try (HttpClient client = HttpTestSupport.newClient())
        {
            String login = HttpTestSupport.uniqueLogin("bad");
            http.registerAndLogin(client, login);

            // One case per distinct server path: business validation, unknown property,
            // malformed JSON, paging validation and parameter type mismatch.
            assertBadRequest(http.patch(client, ME, "{}"));
            assertBadRequest(http.patch(client, ME, "{\"email\":\"secret-not-an-email\"}"));
            assertBadRequest(http.patch(client, ME, "{\"displayName\":\"Hacker\",\"state\":\"BLOCKED\"}"));
            assertBadRequest(http.patch(client, ME, "{\"displayName\":"));
            assertBadRequest(http.get(client, HISTORY + "?size=101"));
            assertBadRequest(http.get(client, HISTORY + "?page=abc"));

            JsonNode me = http.json(http.get(client, ME));
            assertThat(me.get("login").asString()).isEqualTo(login);
            assertThat(me.get("displayName").asString()).isEqualTo("Sport Fan");
            assertThat(me.get("state").asString()).isEqualTo("ACTIVE");
            assertThat(http.json(http.get(client, HISTORY)).get("totalElements").asInt()).isZero();
        }
    }

    private static void assertBadRequest(HttpResponse<String> response)
    {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                value -> assertThat(value).startsWith("application/problem+json"));
        assertThat(response.body()).contains("Invalid user request.")
                .doesNotContain("secret-not-an-email", "BLOCKED", "trace", "exception");
    }
}
