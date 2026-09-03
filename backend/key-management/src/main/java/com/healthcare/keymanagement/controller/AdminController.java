package com.healthcare.keymanagement.controller;

import com.healthcare.keymanagement.service.KeyExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final KeyExpirationService keyExpirationService;

    @GetMapping("/check")
    public Map<String, Object> adminCheck(
            Authentication authentication
    ) {
        return Map.of(
                "message", "Admin access granted",
                "username", authentication.getName(),
                "role", authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );
    }

    @PostMapping("/keys/expire")
    public Map<String, Object> expireKeys() {

        int expiredCount =
                keyExpirationService.expireKeys();

        return Map.of(
                "message", "Key expiration check completed",
                "expiredKeys", expiredCount
        );
    }
}