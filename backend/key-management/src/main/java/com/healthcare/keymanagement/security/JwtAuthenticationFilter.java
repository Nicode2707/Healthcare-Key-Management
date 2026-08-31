package com.healthcare.keymanagement.security;

import com.healthcare.keymanagement.entity.User;
import com.healthcare.keymanagement.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // =========================================================
        // 1. Request information
        // =========================================================

        System.out.println();
        System.out.println("=================================================");
        System.out.println("JWT FILTER -> " +
                request.getMethod() +
                " " +
                request.getRequestURI());
        System.out.println("=================================================");


        // =========================================================
        // 2. Get Authorization Header
        // =========================================================

        String authHeader =
                request.getHeader("Authorization");

        System.out.println(
                "JWT FILTER -> Authorization Header: "
                        + (authHeader != null
                        ? "PRESENT"
                        : "NOT PRESENT")
        );


        // =========================================================
        // 3. Check Bearer Token
        // =========================================================

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            System.out.println(
                    "JWT FILTER -> No Bearer token found"
            );

            filterChain.doFilter(request, response);
            return;
        }


        // =========================================================
        // 4. Extract Token
        // =========================================================

        String token =
                authHeader.substring(7);

        if (token.isBlank()) {

            System.out.println(
                    "JWT FILTER -> Empty JWT token"
            );

            filterChain.doFilter(request, response);
            return;
        }

        System.out.println(
                "JWT FILTER -> Bearer token received"
        );


        // =========================================================
        // 5. Extract Email from JWT
        // =========================================================

        String email;

        try {

            email =
                    jwtService.extractEmail(token);

            System.out.println(
                    "JWT FILTER -> Email extracted: "
                            + email
            );

        } catch (Exception e) {

            System.out.println(
                    "JWT FILTER -> JWT EXTRACTION FAILED"
            );

            System.out.println(
                    "JWT FILTER -> Error: "
                            + e.getMessage()
            );

            filterChain.doFilter(request, response);
            return;
        }


        // =========================================================
        // 6. Check Existing Authentication
        // =========================================================

        if (SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {


            // =====================================================
            // 7. Load User
            // =====================================================

            UserDetails userDetails;

            try {

                userDetails =
                        userDetailsService
                                .loadUserByUsername(email);

                System.out.println(
                        "JWT FILTER -> User loaded successfully"
                );

            } catch (Exception e) {

                System.out.println(
                        "JWT FILTER -> USER LOAD FAILED"
                );

                System.out.println(
                        "JWT FILTER -> Error: "
                                + e.getMessage()
                );

                filterChain.doFilter(request, response);
                return;
            }


            // =====================================================
            // 8. Check User Type
            // =====================================================

            if (!(userDetails instanceof User user)) {

                System.out.println(
                        "JWT FILTER -> ERROR: UserDetails is not User entity"
                );

                filterChain.doFilter(request, response);
                return;
            }


            // =====================================================
            // 9. Validate JWT
            // =====================================================

            boolean valid;

            try {

                valid =
                        jwtService.isTokenValid(
                                token,
                                user
                        );

            } catch (Exception e) {

                System.out.println(
                        "JWT FILTER -> TOKEN VALIDATION ERROR"
                );

                System.out.println(
                        "JWT FILTER -> Error: "
                                + e.getMessage()
                );

                filterChain.doFilter(request, response);
                return;
            }


            System.out.println(
                    "JWT FILTER -> Token valid: "
                            + valid
            );


            // =====================================================
            // 10. Create Authentication
            // =====================================================

            if (valid) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                        );


                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );


                // =================================================
                // 11. Store Authentication
                // =================================================

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );


                System.out.println(
                        "JWT FILTER -> Authentication SUCCESS"
                );

                System.out.println(
                        "JWT FILTER -> Authorities: "
                                + user.getAuthorities()
                );

            } else {

                System.out.println(
                        "JWT FILTER -> Authentication FAILED"
                );
            }

        } else {

            System.out.println(
                    "JWT FILTER -> Authentication already exists"
            );
        }


        // =========================================================
        // 12. Continue Filter Chain
        // =========================================================

        filterChain.doFilter(request, response);


        // =========================================================
        // 13. Final Authentication Status
        // =========================================================

        System.out.println(
                "JWT FILTER -> Final Authentication: "
                        +
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
        );

        System.out.println(
                "JWT FILTER -> Response Status: "
                        + response.getStatus()
        );

        System.out.println(
                "================================================="
        );
        System.out.println();
    }
}