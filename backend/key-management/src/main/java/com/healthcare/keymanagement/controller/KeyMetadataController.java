package com.healthcare.keymanagement.controller;

import com.healthcare.keymanagement.dto.KeyMetadataResponse;
import com.healthcare.keymanagement.entity.KeyMetadata;
import com.healthcare.keymanagement.service.KeyMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
public class KeyMetadataController {

    private final KeyMetadataService service;

    @PostMapping
    public KeyMetadataResponse create(@RequestBody KeyMetadata keyMetadata) {

        KeyMetadata saved = service.create(keyMetadata);

        return toResponse(saved);
    }

    @GetMapping
    public List<KeyMetadataResponse> getAll() {

        return service.getAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private KeyMetadataResponse toResponse(KeyMetadata key) {

        return new KeyMetadataResponse(
                key.getId(),
                key.getKeyId(),
                key.getAlgorithm(),
                key.getKeyVersion(),
                key.getStatus(),
                key.getCreatedAt(),
                key.getExpiresAt(),
                key.getRevokedAt()
        );
    }
}