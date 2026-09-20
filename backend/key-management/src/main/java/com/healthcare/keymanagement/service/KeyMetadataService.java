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
    private final LifecycleIntegrityService lifecycleIntegrityService;
    private final BlockchainService blockchainService;


    // =========================================================
    // CREATE KEY
    // =========================================================

    @Transactional
    public KeyMetadata create(KeyMetadata keyMetadata) {

        // Generate AES-256 key
        String rawKey =
                aesKeyService.generateAes256Key();

        // Protect key before database storage
        String protectedKey =
                keyProtectionService.protectKey(rawKey);

        keyMetadata.setProtectedKey(protectedKey);

        // Default status
        if (keyMetadata.getStatus() == null) {
            keyMetadata.setStatus(KeyStatus.ACTIVE);
        }

        // First version
        if (keyMetadata.getKeyVersion() == null) {
            keyMetadata.setKeyVersion(1);
        }

        // Creation timestamp
        if (keyMetadata.getCreatedAt() == null) {
            keyMetadata.setCreatedAt(LocalDateTime.now());
        }

        // Save operational data in MySQL
        KeyMetadata savedKey =
                repository.save(keyMetadata);


        // Generate deterministic integrity hash
        String recordHash =
                lifecycleIntegrityService.generateRecordHash(
                        savedKey.getKeyId(),
                        savedKey.getKeyVersion(),
                        "CREATED"
                );


        // Record lifecycle evidence on blockchain
        blockchainService.recordKeyLifecycleEvent(
                savedKey.getKeyId(),
                savedKey.getKeyVersion(),
                0,
                recordHash
        );

        return savedKey;
    }


    // =========================================================
    // GET ALL KEYS
    // =========================================================

    public List<KeyMetadata> getAll() {

        return repository.findAll();
    }


    // =========================================================
    // REVOKE KEY
    // =========================================================

    @Transactional
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


        // Change operational status
        key.setStatus(KeyStatus.REVOKED);
        key.setRevokedAt(LocalDateTime.now());

        KeyMetadata savedKey =
                repository.save(key);


        // Generate integrity evidence
        String recordHash =
                lifecycleIntegrityService.generateRecordHash(
                        savedKey.getKeyId(),
                        savedKey.getKeyVersion(),
                        "REVOKED"
                );


        // Record on blockchain
        blockchainService.recordKeyLifecycleEvent(
                savedKey.getKeyId(),
                savedKey.getKeyVersion(),
                2,
                recordHash
        );

        return savedKey;
    }


    // =========================================================
    // ROTATE KEY
    // =========================================================

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


        // Old version becomes ROTATED
        oldKey.setStatus(KeyStatus.ROTATED);

        repository.save(oldKey);


        // Generate new AES-256 key
        String rawKey =
                aesKeyService.generateAes256Key();

        String protectedKey =
                keyProtectionService.protectKey(rawKey);


        // Create new version
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


        KeyMetadata savedKey =
                repository.save(newKey);


        // Rotation evidence belongs to the new version
        String recordHash =
                lifecycleIntegrityService.generateRecordHash(
                        savedKey.getKeyId(),
                        savedKey.getKeyVersion(),
                        "ROTATED"
                );


        blockchainService.recordKeyLifecycleEvent(
                savedKey.getKeyId(),
                savedKey.getKeyVersion(),
                1,
                recordHash
        );

        return savedKey;
    }


    // =========================================================
    // ARCHIVE KEY
    // =========================================================

    @Transactional
    public int archiveKey(String keyId) {

        List<KeyMetadata> keys =
                repository.findByKeyId(keyId);


        if (keys.isEmpty()) {

            throw new KeyNotFoundException(
                    "Key not found: " + keyId
            );
        }


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


        List<KeyMetadata> keysToArchive =
                keys.stream()
                        .filter(key ->
                                key.getStatus() == KeyStatus.ROTATED
                                        || key.getStatus() == KeyStatus.REVOKED
                                        || key.getStatus() == KeyStatus.EXPIRED
                        )
                        .toList();


        if (keysToArchive.isEmpty()) {

            throw new IllegalStateException(
                    "Key is already archived: " + keyId
            );
        }


        // Change MySQL status
        for (KeyMetadata key : keysToArchive) {
            key.setStatus(KeyStatus.ARCHIVED);
        }

        repository.saveAll(keysToArchive);


        // Blockchain evidence for each transition
        for (KeyMetadata key : keysToArchive) {

            String recordHash =
                    lifecycleIntegrityService.generateRecordHash(
                            key.getKeyId(),
                            key.getKeyVersion(),
                            "ARCHIVED"
                    );

            blockchainService.recordKeyLifecycleEvent(
                    key.getKeyId(),
                    key.getKeyVersion(),
                    3,
                    recordHash
            );
        }


        return keysToArchive.size();
    }
}