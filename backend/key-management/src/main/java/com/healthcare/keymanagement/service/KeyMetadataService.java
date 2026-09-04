package com.healthcare.keymanagement.service;

import com.healthcare.keymanagement.entity.KeyMetadata;
import com.healthcare.keymanagement.entity.KeyStatus;
import com.healthcare.keymanagement.exception.KeyNotFoundException;
import com.healthcare.keymanagement.repository.KeyMetadataRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeyMetadataService {

    private final KeyMetadataRepository repository;
    private final AesKeyService aesKeyService;
    private final KeyProtectionService keyProtectionService;

    public KeyMetadata create(KeyMetadata keyMetadata) {

        String rawKey = aesKeyService.generateAes256Key();

        String protectedKey =
                keyProtectionService.protectKey(rawKey);

        keyMetadata.setProtectedKey(protectedKey);

        if (keyMetadata.getStatus() == null) {
            keyMetadata.setStatus(KeyStatus.ACTIVE);
        }

        if (keyMetadata.getKeyVersion() == null) {
            keyMetadata.setKeyVersion(1);
        }

        if (keyMetadata.getCreatedAt() == null) {
            keyMetadata.setCreatedAt(LocalDateTime.now());
        }

        return repository.save(keyMetadata);
    }

    public List<KeyMetadata> getAll() {
        return repository.findAll();
    }

    public KeyMetadata revokeKey(String keyId) {

        KeyMetadata key =
                repository.findByKeyIdAndStatus(
                        keyId,
                        KeyStatus.ACTIVE
                ).orElseThrow(() ->
                        new KeyNotFoundException(
                                "Active key not found: " + keyId
                        )
                );

        key.setStatus(KeyStatus.REVOKED);
        key.setRevokedAt(LocalDateTime.now());

        return repository.save(key);
    }

    @Transactional
    public KeyMetadata rotateKey(String keyId) {

        KeyMetadata oldKey =
                repository.findByKeyIdAndStatus(
                        keyId,
                        KeyStatus.ACTIVE
                ).orElseThrow(() ->
                        new KeyNotFoundException(
                                "Active key not found: " + keyId
                        )
                );

        oldKey.setStatus(KeyStatus.ROTATED);

        repository.save(oldKey);

        String rawKey =
                aesKeyService.generateAes256Key();

        String protectedKey =
                keyProtectionService.protectKey(rawKey);

        KeyMetadata newKey = new KeyMetadata();

        newKey.setKeyId(oldKey.getKeyId());
        newKey.setAlgorithm(oldKey.getAlgorithm());
        newKey.setKeyVersion(oldKey.getKeyVersion() + 1);
        newKey.setStatus(KeyStatus.ACTIVE);
        newKey.setCreatedAt(LocalDateTime.now());
        newKey.setExpiresAt(oldKey.getExpiresAt());
        newKey.setProtectedKey(protectedKey);

        return repository.save(newKey);
    }

    // ============================================================
    // PHASE 12 — KEY ARCHIVAL
    // ============================================================

    @Transactional
    public int archiveKey(String keyId) {

        List<KeyMetadata> keys =
                repository.findByKeyId(keyId);

        // 1. Key does not exist
        if (keys.isEmpty()) {
            throw new KeyNotFoundException(
                    "Key not found: " + keyId
            );
        }

        // 2. Never archive an ACTIVE key
        boolean activeKeyExists =
                keys.stream()
                        .anyMatch(key ->
                                key.getStatus() == KeyStatus.ACTIVE
                        );

        if (activeKeyExists) {
            throw new IllegalStateException(
                    "Active key cannot be archived: " + keyId
            );
        }

        // 3. Archive historical versions
        int archivedCount = 0;

        for (KeyMetadata key : keys) {

            if (key.getStatus() == KeyStatus.ROTATED
                    || key.getStatus() == KeyStatus.REVOKED
                    || key.getStatus() == KeyStatus.EXPIRED) {

                key.setStatus(KeyStatus.ARCHIVED);

                archivedCount++;
            }
        }

        // 4. Prevent duplicate archival
        if (archivedCount == 0) {
            throw new IllegalStateException(
                    "Key is already archived: " + keyId
            );
        }

        repository.saveAll(keys);

        return archivedCount;
    }
}