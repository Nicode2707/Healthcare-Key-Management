package com.healthcare.keymanagement.service;

import com.healthcare.keymanagement.entity.KeyMetadata;
import com.healthcare.keymanagement.entity.KeyStatus;
import com.healthcare.keymanagement.repository.KeyMetadataRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeyExpirationService {

    private final KeyMetadataRepository keyMetadataRepository;
    private final AuditLogService auditLogService;

    private final LifecycleIntegrityService lifecycleIntegrityService;
    private final BlockchainService blockchainService;


    // =========================================================
    // EXPIRE ACTIVE KEYS
    // =========================================================

    @Transactional
    public int expireKeys() {

        LocalDateTime now =
                LocalDateTime.now();


        // -----------------------------------------------------
        // 1. Find ACTIVE keys whose expiration time has passed
        // -----------------------------------------------------

        List<KeyMetadata> expiredKeys =
                keyMetadataRepository
                        .findByStatusAndExpiresAtLessThanEqual(
                                KeyStatus.ACTIVE,
                                now
                        );


        // -----------------------------------------------------
        // 2. Process each expired key
        // -----------------------------------------------------

        for (KeyMetadata key : expiredKeys) {

            // -------------------------------------------------
            // MySQL lifecycle transition
            // ACTIVE → EXPIRED
            // -------------------------------------------------

            key.setStatus(
                    KeyStatus.EXPIRED
            );


            // -------------------------------------------------
            // Existing audit logging
            // -------------------------------------------------

            auditLogService.log(
                    "SYSTEM",
                    "KEY_EXPIRED " + key.getKeyId()
                            + " VERSION " + key.getKeyVersion(),
                    "SYSTEM",
                    "KEY_EXPIRATION",
                    200
            );


            // -------------------------------------------------
            // Generate deterministic integrity hash
            // -------------------------------------------------

            String recordHash =
                    lifecycleIntegrityService.generateRecordHash(
                            key.getKeyId(),
                            key.getKeyVersion(),
                            "EXPIRED"
                    );


            // -------------------------------------------------
            // Record EXPIRED lifecycle evidence
            // on blockchain
            //
            // EventType:
            // 4 = EXPIRED
            // -------------------------------------------------

            blockchainService.recordKeyLifecycleEvent(
                    key.getKeyId(),
                    key.getKeyVersion(),
                    4,
                    recordHash
            );
        }


        // -----------------------------------------------------
        // 3. Persist MySQL changes
        // -----------------------------------------------------

        keyMetadataRepository.saveAll(
                expiredKeys
        );


        return expiredKeys.size();
    }
}