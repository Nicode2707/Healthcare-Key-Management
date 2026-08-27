package com.healthcare.keymanagement.service;

import com.healthcare.keymanagement.entity.KeyMetadata;
import com.healthcare.keymanagement.repository.KeyMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeyMetadataService {

    private final KeyMetadataRepository repository;
    private final AesKeyService aesKeyService;
    private final KeyProtectionService keyProtectionService;

    public KeyMetadata create(KeyMetadata keyMetadata) {

        // 1. Generate AES-256 key internally
        String rawKey = aesKeyService.generateAes256Key();

        // 2. Protect the AES key
        String protectedKey = keyProtectionService.protect(rawKey);

        // 3. Store ONLY protected key
        keyMetadata.setProtectedKey(protectedKey);

        // 4. Never store rawKey
        return repository.save(keyMetadata);
    }

    public List<KeyMetadata> getAll() {
        return repository.findAll();
    }
}