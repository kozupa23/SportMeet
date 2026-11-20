package cz.osu.sportmeet;

import cz.osu.sportmeet.common.persistence.DataInfoObjectType;
import cz.osu.sportmeet.user.persistence.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers successful registration, uniqueness, validation and public endpoint security.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserRegistrationIntegrationTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Verifies canonical persisted identity, hashing, active state and system audit.
     *
     * @throws Exception if request processing fails unexpectedly
     */
    @Test
    void shouldRegisterActiveUserWithCanonicalLoginAndHashedPassword() throws Exception
    {
        var response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""" 
                                {"login":"  SportsFan ","displayName":"Sport Fan","email":"  SportsFan@Example.COM ","password":"safe-password-123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.login").value("sportsfan"))
                .andExpect(jsonPath("$.displayName").value("Sport Fan"))
                .andExpect(jsonPath("$.email").value("sportsfan@example.com"))
                .andExpect(jsonPath("$.state").value("ACTIVE"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn().getResponse();

        var saved = userRepository.findAll().stream()
                .filter(user -> user.getLogin().equals("sportsfan"))
                .findFirst().orElseThrow();
        assertThat(response.getHeader("Location")).isEqualTo("/api/v1/users/" + saved.getId());
        assertThat(passwordEncoder.matches("safe-password-123", saved.getPasswordHash())).isTrue();
        assertThat(saved.getPasswordHash()).isNotEqualTo("safe-password-123");
        assertThat(saved.getDataInfo().getCreatedByObjectType()).isEqualTo(DataInfoObjectType.SYSTEM);
        assertThat(saved.getDataInfo().getCreatedByObjectId()).isEmpty();
        assertThat(saved.getDataInfo().getModified()).isEmpty();
        assertThat(saved.getVersion()).isNotNull();
    }

    /**
     * Ensures two differently cased logins cannot belong to separate users.
     *
     * @throws Exception if request processing fails unexpectedly
     */
    @Test
    void shouldRejectDuplicateLoginIgnoringCase() throws Exception
    {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"CaseFan","displayName":"Sport Fan","email":"case-one@example.com","password":"safe-password-123"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"CASEFAN","displayName":"Sport Fan","email":"case-two@example.com","password":"safe-password-456"}
                                """))
                .andExpect(status().isConflict());
        assertThat(userRepository.existsByLogin("casefan")).isTrue();
    }

    /**
     * Ensures email addresses also use case-insensitive identity matching.
     *
     * @throws Exception if request processing fails unexpectedly
     */
    @Test
    void shouldRejectDuplicateEmailIgnoringCase() throws Exception
    {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"mailone","displayName":"Sport Fan","email":"Same@Example.com","password":"safe-password-123"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"mailtwo","displayName":"Sport Fan","email":"SAME@example.COM","password":"safe-password-456"}
                                """))
                .andExpect(status().isConflict());
    }

    /**
     * Verifies that an e-mail rejected by business validation (not by Bean Validation)
     * is reported as HTTP 400. Short passwords are covered by {@code UserRegistrationHttpTest}.
     *
     * @throws Exception if request processing fails unexpectedly
     */
    @Test
    void shouldRejectInvalidEmail() throws Exception
    {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"invalidmail","displayName":"Sport Fan","email":"bad-email","password":"safe-password-123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that protected endpoints remain unavailable without authentication.
     *
     * @throws Exception if request processing fails unexpectedly
     */
    @Test
    void shouldRequireAuthenticationForOtherUserOperations() throws Exception
    {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Verifies that the database enforces login uniqueness independently of the service.
     *
     * @throws Exception if request processing fails unexpectedly
     */
    @Test
    void shouldEnforceDatabaseLoginUniqueness() throws Exception
    {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"dbfan","displayName":"Sport Fan","email":"dbfan@example.com","password":"safe-password-123"}
                                """))
                .andExpect(status().isCreated());

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO T_USER (LOGIN, DISPLAY_NAME, EMAIL, PASSWORD_HASH, STATE, VALID, CREATED_AT, CREATED_BY_OBJECT_TYPE, VERSION) "
                        + "VALUES (?, ?, ?, ?, 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'SYSTEM', 0)",
                "dbfan", "Another Fan", "another@example.com", "encoded"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    /**
     * Ensures public registration requires a nonblank display name.
     *
     * @throws Exception if request processing fails unexpectedly
     */
    @Test
    void shouldRejectMissingDisplayName() throws Exception
    {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"missingname","email":"missingname@example.com","password":"safe-password-123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    /**
     * Invalid user rows continue reserving their canonical identity values.
     *
     * @throws Exception if request processing fails unexpectedly
     */
    @Test
    void shouldKeepLoginAndEmailReservedForInvalidUsers() throws Exception
    {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"reservedfan","displayName":"Reserved Fan","email":"reserved@example.com","password":"safe-password-123"}
                                """))
                .andExpect(status().isCreated());

        jdbcTemplate.update("UPDATE T_USER SET VALID = FALSE WHERE LOGIN = ?", "reservedfan");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"RESERVEDFAN","displayName":"Other Fan","email":"other@example.com","password":"safe-password-123"}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"otherfan","displayName":"Other Fan","email":"RESERVED@example.com","password":"safe-password-123"}
                                """))
                .andExpect(status().isConflict());
    }

}
