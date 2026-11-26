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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HTTP integration coverage for the group detail (KAN-19, US-04-02 "Zobrazit detail sportovní skupiny").
 * Members other than the founder are inserted directly, because adding members has no endpoint yet.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GroupDetailHttpTest
{
    private static final String GROUPS = "/api/v1/groups";

    @LocalServerPort
    private int port;

    @Autowired
    private JsonMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HttpTestSupport http;

    @BeforeEach
    void setUp()
    {
        http = new HttpTestSupport(port, mapper);
    }

    /**
     * AC1–AC7: the owner sees basic information and current members with roles, owner first;
     * an ended membership is not listed and neither logins nor e-mail addresses leak.
     */
    @Test
    void ownerSeesGroupWithCurrentMembersOrderedByRole() throws Exception
    {
        try (HttpClient owner = HttpTestSupport.newClient())
        {
            String ownerLogin = HttpTestSupport.uniqueLogin("owner");
            http.registerAndLogin(owner, ownerLogin);
            long groupId = createGroup(owner, "{\"name\":\"Florbal\",\"description\":\"Úterní tréninky\"}");
            String deputy = registerUser("deputy");
            String member = registerUser("member");
            String former = registerUser("former");
            addMembership(groupId, deputy, "SECONDARY_OWNER", false, true);
            addMembership(groupId, member, "MEMBER", false, true);
            addMembership(groupId, former, "MEMBER", true, true);

            HttpResponse<String> response = http.get(owner, GROUPS + "/" + groupId);

            assertThat(response.statusCode()).isEqualTo(200);
            JsonNode group = http.json(response);
            assertThat(group.get("id").asLong()).isEqualTo(groupId);
            assertThat(group.get("name").asString()).isEqualTo("Florbal");
            assertThat(group.get("description").asString()).isEqualTo("Úterní tréninky");
            assertThat(group.get("visibility").asString()).isEqualTo("PRIVATE");
            assertThat(group.get("myRole").asString()).isEqualTo("OWNER");

            JsonNode members = group.get("members");
            assertThat(members).hasSize(3);
            assertThat(members.get(0).get("userId").asLong()).isEqualTo(userId(ownerLogin));
            assertThat(members.get(0).get("role").asString()).isEqualTo("OWNER");
            assertThat(members.get(1).get("userId").asLong()).isEqualTo(userId(deputy));
            assertThat(members.get(1).get("role").asString()).isEqualTo("SECONDARY_OWNER");
            assertThat(members.get(2).get("userId").asLong()).isEqualTo(userId(member));
            assertThat(members.get(2).get("role").asString()).isEqualTo("MEMBER");
            assertThat(members.get(0).get("displayName").asString()).isEqualTo("Sport Fan");
            assertThat(members.get(0).has("login")).isFalse();

            assertThat(response.body())
                    .doesNotContain(ownerLogin, deputy, member, former)
                    .doesNotContain("@");
        }
    }

    /**
     * AC3, AC4, AC8: membership, not visibility, decides about the member list; a regular member of a public
     * group sees the members and learns their own role.
     */
    @Test
    void memberOfPublicGroupSeesMembersAndOwnRole() throws Exception
    {
        try (HttpClient owner = HttpTestSupport.newClient(); HttpClient member = HttpTestSupport.newClient())
        {
            http.registerAndLogin(owner, HttpTestSupport.uniqueLogin("owner"));
            long groupId = createGroup(owner, "{\"name\":\"Volejbal\",\"visibility\":\"PUBLIC\"}");
            String memberLogin = HttpTestSupport.uniqueLogin("member");
            http.registerAndLogin(member, memberLogin);
            addMembership(groupId, memberLogin, "MEMBER", false, true);

            JsonNode group = http.json(http.get(member, GROUPS + "/" + groupId));

            assertThat(group.get("visibility").asString()).isEqualTo("PUBLIC");
            assertThat(group.get("myRole").asString()).isEqualTo("MEMBER");
            assertThat(group.get("members")).hasSize(2);
        }
    }

    /**
     * AC1, AC2, AC8: a non-member sees only basic information of a public group, without its members.
     */
    @Test
    void nonMemberSeesOnlyBasicInformationOfPublicGroup() throws Exception
    {
        try (HttpClient owner = HttpTestSupport.newClient(); HttpClient stranger = HttpTestSupport.newClient())
        {
            http.registerAndLogin(owner, HttpTestSupport.uniqueLogin("owner"));
            long groupId = createGroup(owner, "{\"name\":\"Běh\",\"visibility\":\"PUBLIC\"}");
            http.registerAndLogin(stranger, HttpTestSupport.uniqueLogin("stranger"));

            HttpResponse<String> response = http.get(stranger, GROUPS + "/" + groupId);

            assertThat(response.statusCode()).isEqualTo(200);
            JsonNode group = http.json(response);
            assertThat(group.get("name").asString()).isEqualTo("Běh");
            assertThat(group.get("visibility").asString()).isEqualTo("PUBLIC");
            assertThat(group.get("myRole").isNull()).isTrue();
            assertThat(group.get("members").isNull()).isTrue();
        }
    }

    /**
     * AC7–AC10: a private group seen by a non-member, a former member or a member whose membership record
     * was invalidated, an invalidated group seen by its owner, and a group that does not exist
     * all produce the same 404 without revealing anything.
     */
    @Test
    void hiddenInvalidatedAndMissingGroupsAreIndistinguishable() throws Exception
    {
        try (HttpClient owner = HttpTestSupport.newClient();
             HttpClient stranger = HttpTestSupport.newClient();
             HttpClient former = HttpTestSupport.newClient();
             HttpClient invalidMember = HttpTestSupport.newClient())
        {
            http.registerAndLogin(owner, HttpTestSupport.uniqueLogin("owner"));
            long privateGroup = createGroup(owner, "{\"name\":\"Tajný klub\",\"visibility\":\"PRIVATE\"}");
            long invalidatedGroup = createGroup(owner, "{\"name\":\"Zrušená\",\"visibility\":\"PUBLIC\"}");
            jdbcTemplate.update("UPDATE T_GROUP SET VALID = FALSE WHERE ID = ?", invalidatedGroup);
            http.registerAndLogin(stranger, HttpTestSupport.uniqueLogin("stranger"));
            String formerLogin = HttpTestSupport.uniqueLogin("former");
            http.registerAndLogin(former, formerLogin);
            addMembership(privateGroup, formerLogin, "MEMBER", true, true);
            String invalidMemberLogin = HttpTestSupport.uniqueLogin("invalid");
            http.registerAndLogin(invalidMember, invalidMemberLogin);
            addMembership(privateGroup, invalidMemberLogin, "MEMBER", false, false);

            List<HttpResponse<String>> responses = List.of(
                    http.get(stranger, GROUPS + "/" + privateGroup),
                    http.get(former, GROUPS + "/" + privateGroup),
                    http.get(invalidMember, GROUPS + "/" + privateGroup),
                    http.get(owner, GROUPS + "/" + invalidatedGroup),
                    http.get(stranger, GROUPS + "/" + Long.MAX_VALUE));

            for (HttpResponse<String> response : responses)
            {
                assertThat(response.statusCode()).isEqualTo(404);
                JsonNode problem = http.json(response);
                assertThat(problem.get("title").asString()).isEqualTo("Group not found");
                assertThat(problem.get("detail").asString()).isEqualTo("Group not found.");
                assertThat(response.body()).doesNotContain("Tajný klub", "Zrušená");
            }
        }
    }

    @Test
    void requiresAuthenticationAndNumericId() throws Exception
    {
        try (HttpClient anonymous = HttpTestSupport.newClient(); HttpClient client = HttpTestSupport.newClient())
        {
            assertThat(http.get(anonymous, GROUPS + "/1").statusCode()).isEqualTo(401);

            http.registerAndLogin(client, HttpTestSupport.uniqueLogin("user"));
            HttpResponse<String> response = http.get(client, GROUPS + "/abc");
            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(http.json(response).get("detail").asString()).isEqualTo("Group identifier must be a number.");
        }
    }

    private long createGroup(HttpClient client, String body) throws Exception
    {
        HttpResponse<String> response = http.send(client, "POST", GROUPS, body, http.csrf(client));
        assertThat(response.statusCode()).isEqualTo(201);

        return http.json(response).get("id").asLong();
    }

    private String registerUser(String prefix) throws Exception
    {
        String login = HttpTestSupport.uniqueLogin(prefix);
        try (HttpClient client = HttpTestSupport.newClient())
        {
            http.register(client, login, HttpTestSupport.DEFAULT_PASSWORD);
        }

        return login;
    }

    private long userId(String login)
    {
        Long id = jdbcTemplate.queryForObject("SELECT ID FROM T_USER WHERE LOGIN = ?", Long.class, login);
        assertThat(id).isNotNull();

        return id;
    }

    /**
     * Inserts a membership directly; adding members is a later story without an endpoint yet.
     */
    private void addMembership(long groupId, String login, String role, boolean ended, boolean valid)
    {
        long userId = userId(login);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        jdbcTemplate.update("INSERT INTO T_GROUP_MEMBERSHIP (GROUP_ID, USER_ID, ROLE, STATUS, ENDED_AT, VALID, "
                        + "CREATED_AT, CREATED_BY_OBJECT_ID, CREATED_BY_OBJECT_TYPE, VERSION) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'USER', 0)",
                groupId, userId, role, ended ? "ENDED" : "ACTIVE", ended ? now : null, valid, now.minusDays(1),
                userId);
    }
}
