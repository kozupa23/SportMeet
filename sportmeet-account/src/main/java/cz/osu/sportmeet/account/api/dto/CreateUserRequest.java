package cz.osu.sportmeet.account.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NullMarked;

/**
 * Defines the request payload for public user registration.
 *
 * @param login       requested unique account login
 * @param displayName visible name
 * @param email       requested email address
 * @param password    plaintext password to encode
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record CreateUserRequest(
        @NotBlank String login,
        @NotBlank @Size(max = 255) String displayName,
        @NotBlank @Size(max = 254) String email,
        @NotBlank @Size(min = 8) String password)
{
}