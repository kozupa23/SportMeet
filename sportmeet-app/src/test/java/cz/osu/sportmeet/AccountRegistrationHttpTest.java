package cz.osu.sportmeet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccountRegistrationHttpTest
{
    @LocalServerPort
    private int port;

    @Autowired
    private Environment environment;

    @Test
    void shouldReturnBadRequestForShortPassword() throws Exception
    {
        HttpRequest request = HttpRequest.newBuilder(uri("/api/v1/accounts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"login":"shortpass","displayName":"Sport Fan","email":"short@example.com","password":"x7!"}
                        """))
                .build();

        try (HttpClient client = HttpClient.newHttpClient())
        {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(response.headers().firstValue("Content-Type"))
                    .hasValueSatisfying(value -> assertThat(value).startsWith("application/problem+json"));
            assertThat(response.body())
                    .contains("\"title\":\"Invalid registration data\"",
                            "\"detail\":\"Registration data failed validation.\"")
                    .doesNotContain("x7!", "trace", "rejectedValue");
        }
    }

    @Test
    void shouldNotExposeErrorDetailsForMalformedRequests() throws Exception
    {
        assertThat(environment.getProperty("spring.web.error.include-stacktrace")).isEqualTo("never");
        assertThat(environment.getProperty("spring.web.error.include-message")).isEqualTo("never");
        assertThat(environment.getProperty("spring.web.error.include-binding-errors")).isEqualTo("never");
        assertThat(environment.getProperty("spring.web.error.include-exception")).isEqualTo("false");

        HttpRequest request = HttpRequest.newBuilder(uri("/api/v1/accounts?trace=true&message=true&errors=true"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"password\":\"secret-value\","))
                .build();

        try (HttpClient client = HttpClient.newHttpClient())
        {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(response.body()).doesNotContain(
                    "secret-value", "\"trace\"", "\"message\"", "\"errors\"", "\"exception\"");
        }
    }

    @Test
    void shouldDenyDirectRequestsToErrorEndpoint() throws Exception
    {
        HttpRequest request = HttpRequest.newBuilder(uri("/error")).GET().build();

        try (HttpClient client = HttpClient.newHttpClient())
        {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(403);
        }
    }

    private URI uri(String path)
    {
        return URI.create("http://localhost:" + port + path);
    }
}
