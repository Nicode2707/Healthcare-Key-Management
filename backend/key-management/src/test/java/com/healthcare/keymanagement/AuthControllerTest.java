package com.healthcare.keymanagement;

import com.healthcare.keymanagement.controller.AuthController;
import com.healthcare.keymanagement.dto.LoginRequest;
import com.healthcare.keymanagement.dto.RegisterRequest;
import com.healthcare.keymanagement.entity.Role;
import com.healthcare.keymanagement.entity.User;
import com.healthcare.keymanagement.repository.UserRepository;
import com.healthcare.keymanagement.security.JwtService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController controller;

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("testuser"))
                .thenReturn(false);

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("ENCODED-PASSWORD");

        User savedUser = User.builder()
                .id(100L)
                .username("testuser")
                .email("test@example.com")
                .password("ENCODED-PASSWORD")
                .role(Role.USER)
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        ResponseEntity<?> response =
                controller.register(request);

        assertEquals(
                201,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        Map<?, ?> body =
                (Map<?, ?>) response.getBody();

        assertEquals(
                "User registered successfully",
                body.get("message")
        );

        assertEquals(
                100L,
                body.get("userId")
        );

        assertEquals(
                "testuser",
                body.get("username")
        );

        assertEquals(
                "USER",
                body.get("role")
        );

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateUsername() {

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername("existinguser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername(
                "existinguser"
        )).thenReturn(true);

        ResponseEntity<?> response =
                controller.register(request);

        assertEquals(
                409,
                response.getStatusCode().value()
        );

        Map<?, ?> body =
                (Map<?, ?>) response.getBody();

        assertEquals(
                "Username already exists",
                body.get("message")
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser"))
                .thenReturn(false);

        when(userRepository.existsByEmail(
                "existing@example.com"
        )).thenReturn(true);

        ResponseEntity<?> response =
                controller.register(request);

        assertEquals(
                409,
                response.getStatusCode().value()
        );

        Map<?, ?> body =
                (Map<?, ?>) response.getBody();

        assertEquals(
                "Email already exists",
                body.get("message")
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request =
                new LoginRequest();

        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .id(100L)
                .username("testuser")
                .email("test@example.com")
                .password("ENCODED-PASSWORD")
                .role(Role.USER)
                .build();

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn("TEST-JWT-TOKEN");

        ResponseEntity<?> response =
                controller.login(request);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        Map<?, ?> body =
                (Map<?, ?>) response.getBody();

        assertEquals(
                "Login successful",
                body.get("message")
        );

        assertEquals(
                "TEST-JWT-TOKEN",
                body.get("token")
        );

        assertEquals(
                "Bearer",
                body.get("type")
        );

        assertEquals(
                "24 hours",
                body.get("expiresIn")
        );

        verify(authenticationManager)
                .authenticate(
                        any(UsernamePasswordAuthenticationToken.class)
                );

        verify(jwtService)
                .generateToken(user);
    }
}