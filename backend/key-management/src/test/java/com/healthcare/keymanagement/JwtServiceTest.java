package com.healthcare.keymanagement;

import com.healthcare.keymanagement.entity.Role;
import com.healthcare.keymanagement.entity.User;
import com.healthcare.keymanagement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private final String secret =
            "VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRLZXlGb3JUZXN0aW5nMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";

    @BeforeEach
    void setUp() {

        jwtService =
                new JwtService(
                        secret,
                        86400000L
                );
    }

    @Test
    void shouldGenerateTokenSuccessfully() {

        User user = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@test.com")
                .password("password")
                .role(Role.ADMIN)
                .build();

        String token =
                jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractEmailFromToken() {

        User user = User.builder()
                .id(2L)
                .username("user")
                .email("user@test.com")
                .password("password")
                .role(Role.USER)
                .build();

        String token =
                jwtService.generateToken(user);

        String email =
                jwtService.extractEmail(token);

        assertEquals(
                "user@test.com",
                email
        );
    }

    @Test
    void shouldValidateCorrectToken() {

        User user = User.builder()
                .id(3L)
                .username("admin")
                .email("admin@test.com")
                .password("password")
                .role(Role.ADMIN)
                .build();

        String token =
                jwtService.generateToken(user);

        assertTrue(
                jwtService.isTokenValid(
                        token,
                        user
                )
        );
    }

    @Test
    void shouldRejectTokenForDifferentUser() {

        User originalUser = User.builder()
                .id(4L)
                .username("user1")
                .email("user1@test.com")
                .password("password")
                .role(Role.USER)
                .build();

        User differentUser = User.builder()
                .id(5L)
                .username("user2")
                .email("user2@test.com")
                .password("password")
                .role(Role.USER)
                .build();

        String token =
                jwtService.generateToken(originalUser);

        assertFalse(
                jwtService.isTokenValid(
                        token,
                        differentUser
                )
        );
    }

    @Test
    void shouldRejectInvalidToken() {

        User user = User.builder()
                .id(6L)
                .username("user")
                .email("user@test.com")
                .password("password")
                .role(Role.USER)
                .build();

        assertFalse(
                jwtService.isTokenValid(
                        "invalid.jwt.token",
                        user
                )
        );
    }
}