package cz.osu.sportmeet.config;

import jakarta.servlet.DispatcherType;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures session authentication, authorization and CSRF for SportMeet.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@Configuration
@EnableMethodSecurity
public class SecurityConfiguration
{
    /**
     * Configures authentication, authorization, CSRF and session management.
     *
     * @param http Spring Security HTTP configuration builder
     * @return configured security filter chain
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
    {
        http.authorizeHttpRequests(requests -> requests
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/accounts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .anyRequest().authenticated());

        http.csrf(csrf -> csrf.ignoringRequestMatchers(request ->
                "POST".equals(request.getMethod())
                        && (request.getContextPath() + "/api/v1/accounts").equals(request.getRequestURI())));

        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId));

        http.formLogin(form -> form
                .loginPage("/api/v1/auth/login")
                .loginProcessingUrl("/api/v1/auth/login")
                .usernameParameter("login")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> response.setStatus(204))
                .failureHandler((request, response, exception) -> response.setStatus(401)));

        http.logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)));

        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, exception) -> response.setStatus(401)));

        http.requestCache(AbstractHttpConfigurer::disable);
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