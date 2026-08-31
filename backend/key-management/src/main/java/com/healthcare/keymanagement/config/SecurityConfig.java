package com.healthcare.keymanagement.config;

import com.healthcare.keymanagement.security.JwtAuthenticationFilter;
import com.healthcare.keymanagement.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    // =========================================================
    // 1. Password Encoder
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // =========================================================
    // 2. Authentication Provider
    // =========================================================

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }


    // =========================================================
    // 3. Authentication Manager
    // =========================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }


    // =========================================================
    // 4. Authentication Entry Point
    // =========================================================
    // This handles requests where authentication is missing
    // or authentication failed.
    //
    // Instead of confusing 403, we return 401 Unauthorized.

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, exception) -> {

            response.setStatus(HttpServletResponseStatus.UNAUTHORIZED);

            response.setContentType("application/json");

            response.getWriter().write(
                    """
                    {
                        "status": 401,
                        "error": "Unauthorized",
                        "message": "Authentication is required"
                    }
                    """
            );
        };
    }


    // =========================================================
    // 5. Access Denied Handler
    // =========================================================
    // User is authenticated but does not have permission.

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {

        return (request, response, exception) -> {

            response.setStatus(HttpServletResponseStatus.FORBIDDEN);

            response.setContentType("application/json");

            response.getWriter().write(
                    """
                    {
                        "status": 403,
                        "error": "Forbidden",
                        "message": "You do not have permission to access this resource"
                    }
                    """
            );
        };
    }


    // =========================================================
    // 6. Security Filter Chain
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // -------------------------------------------------
                // CSRF
                // -------------------------------------------------

                .csrf(csrf -> csrf.disable())


                // -------------------------------------------------
                // Session Management
                // -------------------------------------------------

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // -------------------------------------------------
                // Authentication Provider
                // -------------------------------------------------

                .authenticationProvider(
                        authenticationProvider()
                )


                // -------------------------------------------------
                // Exception Handling
                // -------------------------------------------------

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                authenticationEntryPoint()
                        )

                        .accessDeniedHandler(
                                accessDeniedHandler()
                        )
                )


                // -------------------------------------------------
                // Authorization
                // -------------------------------------------------

                .authorizeHttpRequests(auth -> auth

                        // Public APIs
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Protected Key APIs
                        .requestMatchers(
                                "/api/keys/**"
                        ).authenticated()

                        // Everything else
                        .anyRequest().authenticated()
                )


                // -------------------------------------------------
                // JWT Filter
                // -------------------------------------------------

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }


    // =========================================================
    // Small constants for HTTP status codes
    // =========================================================

    private static class HttpServletResponseStatus {

        private static final int UNAUTHORIZED = 401;

        private static final int FORBIDDEN = 403;
    }
}