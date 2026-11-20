package cz.osu.sportmeet.support;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shared helpers for black-box HTTP tests: registration, CSRF handling, login and JSON requests.
 * Each {@link HttpClient} created by {@link #newClient()} keeps its own session cookies.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public final class HttpTestSupport
{
    public static final String DEFAULT_PASSWORD = "SafePassword-123!";

    private final int port;
    private final JsonMapper mapper;

    /**
     * Creates HTTP testing utilities for a server port and JSON mapper.
     *
     * @param port   port of the test server
     * @param mapper mapper used for JSON serialization and parsing
     */
    public HttpTestSupport(int port, JsonMapper mapper)
    {
        this.port = port;
        this.mapper = mapper;
    }

    /**
     * Creates a test HTTP client with an isolated cookie store.
     *
     * @return HTTP client retaining its own session cookies
     */
    public static HttpClient newClient()
    {
        return HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .build();
    }

    /**
     * Returns a unique, valid login with the given prefix.
     */
    public static String uniqueLogin(String prefix)
    {
        return prefix + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Registers an user with display name "Sport Fan" and e-mail {@code <login>@example.com}.
     */
    public void register(HttpClient client, String login, String password) throws Exception
    {
        String json = mapper.writeValueAsString(Map.of(
                "login", login,
                "displayName", "Sport Fan",
                "email", login + "@example.com",
                "password", password));
        assertThat(send(client, "POST", "/api/v1/users", json, null).statusCode()).isEqualTo(201);
    }

    /**
     * Registers an user and logs the client in.
     */
    public void registerAndLogin(HttpClient client, String login) throws Exception
    {
        register(client, login, DEFAULT_PASSWORD);
        assertThat(login(client, login, DEFAULT_PASSWORD).statusCode()).isEqualTo(204);
    }

    /**
     * Authenticates the test client using a fresh CSRF token.
     *
     * @param client   HTTP client with its session cookies
     * @param login    user login
     * @param password user password
     * @return login HTTP response
     * @throws Exception if the request fails
     */
    public HttpResponse<String> login(HttpClient client, String login, String password) throws Exception
    {
        return postLoginForm(client, login, password, csrf(client));
    }

    /**
     * Submits a login request without a CSRF token for security tests.
     *
     * @param client   HTTP client with its session cookies
     * @param login    user login
     * @param password user password
     * @return login HTTP response
     * @throws Exception if the request fails
     */
    public HttpResponse<String> loginWithoutCsrf(HttpClient client, String login, String password) throws Exception
    {
        return postLoginForm(client, login, password, null);
    }

    /**
     * Logs out the test client using a valid CSRF token.
     *
     * @param client authenticated HTTP client
     * @return logout HTTP response
     * @throws Exception if the request fails
     */
    public HttpResponse<String> logout(HttpClient client) throws Exception
    {
        Header csrf = csrf(client);
        HttpRequest request = HttpRequest.newBuilder(uri("/api/v1/auth/logout"))
                .header(csrf.name(), csrf.value())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postLoginForm(HttpClient client, String login, String password,
                                               @Nullable Header csrf) throws Exception
    {
        String form = "login=" + encode(login) + "&password=" + encode(password);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri("/api/v1/auth/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form));
        if (csrf != null)
        {
            builder.header(csrf.name(), csrf.value());
        }

        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Fetches a fresh CSRF token bound to the client's session.
     */
    public Header csrf(HttpClient client) throws Exception
    {
        HttpResponse<String> response = send(client, "GET", "/api/v1/auth/csrf", null, null);
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode token = json(response);

        return new Header(token.get("headerName").asString(), token.get("token").asString());
    }

    /**
     * Sends a GET request to a test server endpoint.
     *
     * @param client HTTP client
     * @param path   request path
     * @return GET HTTP response
     * @throws Exception if the request fails
     */
    public HttpResponse<String> get(HttpClient client, String path) throws Exception
    {
        return send(client, "GET", path, null, null);
    }

    /**
     * Sends a JSON PATCH request including a fresh CSRF token.
     */
    public HttpResponse<String> patch(HttpClient client, String path, String body) throws Exception
    {
        return send(client, "PATCH", path, body, csrf(client));
    }

    /**
     * Sends an HTTP request with optional JSON body and CSRF header.
     *
     * @param client HTTP client
     * @param method HTTP method
     * @param path   request path
     * @param body   request body, or null for no body
     * @param header additional CSRF header, or null
     * @return HTTP response
     * @throws Exception if the request fails
     */
    public HttpResponse<String> send(HttpClient client, String method, String path,
                                     @Nullable String body, @Nullable Header header) throws Exception
    {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .method(method, body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (header != null)
        {
            builder.header(header.name(), header.value());
        }

        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Parses the response body into a JSON node.
     *
     * @param response HTTP response containing JSON
     * @return parsed JSON response body
     */
    public JsonNode json(HttpResponse<String> response)
    {
        return mapper.readTree(response.body());
    }

    private URI uri(String path)
    {
        return URI.create("http://localhost:" + port + path);
    }

    private static String encode(String value)
    {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Name and value of a CSRF header used by HTTP test requests.
     *
     * @param name  HTTP header name
     * @param value HTTP header value
     * @author Pavel Kozumplik
     */
    public record Header(String name, String value)
    {
    }
}
