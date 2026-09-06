package com.healthcare.keymanagement.controller;

import com.healthcare.keymanagement.dto.LoginRequest;
import com.healthcare.keymanagement.dto.RegisterRequest;
import com.healthcare.keymanagement.entity.Role;
import com.healthcare.keymanagement.entity.User;
import com.healthcare.keymanagement.repository.UserRepository;
import com.healthcare.keymanagement.security.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "User registration and JWT authentication APIs"
)
public class AuthController {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;


    // =========================================================
    // REGISTER
    // =========================================================

    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a new application user.
                    All newly registered users are assigned the USER role.
                    Passwords are securely hashed before storage.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email already exists",
                    content = @Content
            )
    })
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        if (userRepository.existsByUsername(request.getUsername())) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "Username already exists"
                    ));
        }

        if (userRepository.existsByEmail(request.getEmail())) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "Email already exists"
                    ));
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .role(Role.USER)
                .build();

        User savedUser =
                userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message",
                        "User registered successfully",

                        "userId",
                        savedUser.getId(),

                        "username",
                        savedUser.getUsername(),

                        "email",
                        savedUser.getEmail(),

                        "role",
                        savedUser.getRole().name()
                ));
    }


    // =========================================================
    // LOGIN
    // =========================================================

    @Operation(
            summary = "Login and obtain JWT token",
            description = """
                    Authenticates a registered user using email
                    and password and returns a JWT Bearer token.
                    The token is required for protected APIs.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content
            )
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
    ) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        User user =
                (User) authentication.getPrincipal();

        String token =
                jwtService.generateToken(user);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Login successful",

                        "token",
                        token,

                        "type",
                        "Bearer",

                        "expiresIn",
                        "24 hours"
                )
        );
    }
}