package com.septeo.ulyses.technical.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security configuration for the API.
 * GET endpoints are public; POST, PUT and DELETE endpoints require an authenticated user with the ADMIN role.
 */
@Configuration
public class SecurityConfig {

    private static final String API_USERNAME = "admin";
    private static final String API_PASSWORD = "admin123";
    private static final String API_ROLE = "ADMIN";

    /**
     * Defines the security filter chain.
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET).permitAll()
                        .anyRequest().hasRole(API_ROLE))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(withDefaults());
        return http.build();
    }

    /**
     * Provides an in-memory user used to authenticate write operations.
     *
     * @param passwordEncoder the encoder used to store the user password
     * @return the {@link UserDetailsService} holding the API user
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername(API_USERNAME)
                .password(passwordEncoder.encode(API_PASSWORD))
                .roles(API_ROLE)
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Provides the password encoder used for the in-memory user.
     *
     * @return a BCrypt-based {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
