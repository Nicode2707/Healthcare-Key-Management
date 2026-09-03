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

    @Transactional
    public int expireKeys() {

        LocalDateTime now = LocalDateTime.now();

        List<KeyMetadata> expiredKeys =
                keyMetadataRepository
                        .findByStatusAndExpiresAtLessThanEqual(
                                KeyStatus.ACTIVE,
                                now
                        );

        for (KeyMetadata key : expiredKeys) {

            key.setStatus(KeyStatus.EXPIRED);

            auditLogService.log(
                    "SYSTEM",
                    "KEY_EXPIRED " + key.getKeyId()
                            + " VERSION " + key.getKeyVersion(),
                    "SYSTEM",
                    "KEY_EXPIRATION",
                    200
            );
        }

        keyMetadataRepository.saveAll(expiredKeys);

        return expiredKeys.size();
    }
}