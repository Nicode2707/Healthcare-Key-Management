package com.healthcare.keymanagement.service;

import com.healthcare.keymanagement.exception.KeyNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import com.healthcare.keymanagement.entity.KeyMetadata;
import com.healthcare.keymanagement.entity.KeyStatus;
import com.healthcare.keymanagement.repository.KeyMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                        )
                        .orElseThrow(() ->
                                new KeyNotFoundException(
                                        "Active key not found: " + keyId
                                ));

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
                        )
                        .orElseThrow(() ->
                                new KeyNotFoundException(
                                        "Active key not found: " + keyId
                                ));

        oldKey.setStatus(KeyStatus.ROTATED);

        repository.save(oldKey);

        String rawKey =
                aesKeyService.generateAes256Key();

        String protectedKey =
                keyProtectionService.protectKey(rawKey);

        KeyMetadata newKey =
                new KeyMetadata();

        newKey.setKeyId(oldKey.getKeyId());
        newKey.setAlgorithm(oldKey.getAlgorithm());

        newKey.setKeyVersion(
                oldKey.getKeyVersion() + 1
        );

        newKey.setStatus(KeyStatus.ACTIVE);
        newKey.setCreatedAt(LocalDateTime.now());
        newKey.setExpiresAt(oldKey.getExpiresAt());
        newKey.setProtectedKey(protectedKey);

        return repository.save(newKey);
    }
   }