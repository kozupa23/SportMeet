package cz.osu.sportmeet.config;

import jakarta.servlet.DispatcherType;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures security infrastructure for SportMeet.
 * Public registration is allowed without authentication.
 * Other endpoints remain protected until authentication is implemented.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Configuration
public class SecurityConfiguration
{
    /**
     * Configures authorization and CSRF protection.
     *
     * @param http Spring Security HTTP configuration builder
     * @return configured security filter chain
     * @throws Exception if security configuration fails
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http.authorizeHttpRequests(registry -> registry
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/accounts").permitAll()
                .anyRequest().denyAll());

        http.csrf(csrf -> csrf
                .ignoringRequestMatchers(request ->
                        "POST".equals(request.getMethod())
                                && (request.getContextPath() + "/api/v1/accounts")
                                .equals(request.getRequestURI())
                )
        );

        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * Provides an adaptive password encoder for account credentials.
     *
     * @return BCrypt password encoder
     */
    @Bean
    PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}