package cz.osu.sportmeet;

import cz.osu.sportmeet.group.domain.MembershipRole;
import cz.osu.sportmeet.group.domain.MembershipStatus;
import cz.osu.sportmeet.group.persistence.repository.MembershipRepository;
import cz.osu.sportmeet.support.HttpTestSupport;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HTTP integration coverage for founding a group (KAN-19, US "Založit sportovní skupinu").
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GroupCreationHttpTest
{
    private static final String GROUPS = "/api/v1/groups";
    private static final String ME = "/api/v1/users/me";

    @LocalServerPort
    private int port;

    @Autowired
    private JsonMapper mapper;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HttpTestSupport http;

    @BeforeEach
    void setUp()
    {
        http = new HttpTestSupport(port, mapper);
    }

    /**
     * AC5, AC6, AC7: a valid group is created and its founder becomes the only active owner.
     */
    @Test
    void signedInUserFoundsGroupAndBecomesItsOnlyOwner() throws Exception
    {
        try (HttpClient client = HttpTestSupport.newClient())
        {
            http.registerAndLogin(client, HttpTestSupport.uniqueLogin("owner"));
            long userId = http.json(http.get(client, ME)).get("id").asLong();

            // Czech diacritics must survive the whole round trip (JSON parsing, database, response).
            HttpResponse<String> response = post(client, "{\"name\":\"  Florbal Ostrava – Poruba \","
                    + "\"description\":\"Pravidelné tréninky každé úterý\",\"visibility\":\"PUBLIC\"}");

            assertThat(response.statusCode()).isEqualTo(201);
            JsonNode group = http.json(response);
            long groupId = group.get("id").asLong();
            assertThat(response.headers().firstValue("Location")).contains(GROUPS + "/" + groupId);
            assertThat(group.get("name").asString()).isEqualTo("Florbal Ostrava – Poruba");
            assertThat(group.get("description").asString()).isEqualTo("Pravidelné tréninky každé úterý");
            assertThat(group.get("visibility").asString()).isEqualTo("PUBLIC");
            assertThat(group.has("valid")).isFalse();

            assertThat(membershipRepository.findByGroupId(groupId)).singleElement().satisfies(membership ->
            {
                assertThat(membership.getUserId()).isEqualTo(userId);
                assertThat(membership.getRole()).isEqualTo(MembershipRole.OWNER);
                assertThat(membership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
                assertThat(membership.isValid()).isTrue();
            });
        }
    }

    /**
     * AC3, AC4: description is optional and visibility defaults to PRIVATE.
     */
    @Test
    void descriptionIsOptionalAndVisibilityDefaultsToPrivate() throws Exception
    {
        try (HttpClient client = HttpTestSupport.newClient())
        {
            http.registerAndLogin(client, HttpTestSupport.uniqueLogin("owner"));

            HttpResponse<String> response = post(client, "{\"name\":\"Volejbal\"}");

            assertThat(response.statusCode()).isEqualTo(201);
            JsonNode group = http.json(response);
            assertThat(group.get("description").isNull()).isTrue();
            assertThat(group.get("visibility").asString()).isEqualTo("PRIVATE");
        }
    }

    /**
     * AC1: only a signed-in user may found a group, and state-changing requests require CSRF.
     */
    @Test
    void foundingRequiresAuthenticationAndCsrf() throws Exception
    {
        try (HttpClient anonymous = HttpTestSupport.newClient(); HttpClient client = HttpTestSupport.newClient())
        {
            assertThat(post(anonymous, "{\"name\":\"Anonymous group\"}").statusCode()).isEqualTo(401);

            http.registerAndLogin(client, HttpTestSupport.uniqueLogin("owner"));
            assertThat(http.send(client, "POST", GROUPS, "{\"name\":\"No CSRF group\"}", null).statusCode())
                    .isEqualTo(403);
        }
    }

    /**
     * AC1: a user blocked after signing in still has a session, but may no longer found groups.
     */
    @Test
    void userBlockedAfterSigningInCannotFoundGroup() throws Exception
    {
        try (HttpClient client = HttpTestSupport.newClient())
        {
            String login = HttpTestSupport.uniqueLogin("blocked");
            http.registerAndLogin(client, login);
            long userId = http.json(http.get(client, ME)).get("id").asLong();
            jdbcTemplate.update("UPDATE T_USER SET STATE = 'BLOCKED' WHERE LOGIN = ?", login);

            HttpResponse<String> response = post(client, "{\"name\":\"Blocked group\"}");

            assertThat(response.statusCode()).isEqualTo(403);
            assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                    value -> assertThat(value).startsWith("application/problem+json"));
            assertThat(groupsCreatedBy(userId)).isZero();
        }
    }

    /**
     * AC2, AC9: invalid data is rejected with Problem Details and no group is created.
     */
    @Test
    void invalidDataIsRejectedWithoutCreatingGroup() throws Exception
    {
        try (HttpClient client = HttpTestSupport.newClient())
        {
            http.registerAndLogin(client, HttpTestSupport.uniqueLogin("invalid"));
            long userId = http.json(http.get(client, ME)).get("id").asLong();

            // One case per distinct server path: Bean Validation, business rules and JSON parsing.
            assertBadRequest(post(client, "{}"));
            assertBadRequest(post(client, "{\"name\":\"   \"}"));
            assertBadRequest(post(client, "{\"name\":\"" + "x".repeat(101) + "\"}"));
            assertBadRequest(post(client, "{\"name\":\"Volejbal\",\"description\":\"" + "x".repeat(2001) + "\"}"));
            assertBadRequest(post(client, "{\"name\":\"Volejbal\",\"visibility\":\"SECRET\"}"));
            assertBadRequest(post(client, "{\"name\":"));

            assertThat(groupsCreatedBy(userId)).isZero();
        }
    }

    private HttpResponse<String> post(HttpClient client, String body) throws Exception
    {
        return http.send(client, "POST", GROUPS, body, http.csrf(client));
    }

    private int groupsCreatedBy(long userId)
    {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM T_GROUP WHERE CREATED_BY_OBJECT_ID = ?", Integer.class, userId);

        return count == null ? 0 : count;
    }

    private static void assertBadRequest(HttpResponse<String> response)
    {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                value -> assertThat(value).startsWith("application/problem+json"));
        assertThat(response.body()).contains("Invalid group data")
                .doesNotContain("SECRET", "trace", "exception");
    }
}
