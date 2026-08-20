package com.healthcare.keymanagement.controller;

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
    public KeyMetadata create(@RequestBody KeyMetadata keyMetadata) {
        return service.create(keyMetadata);
    }

    @GetMapping
    public List<KeyMetadata> getAll() {
        return service.getAll();
    }
}