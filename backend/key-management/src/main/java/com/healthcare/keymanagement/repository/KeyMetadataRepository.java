package com.healthcare.keymanagement.repository;

import com.healthcare.keymanagement.entity.KeyMetadata;
import com.healthcare.keymanagement.entity.KeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface KeyMetadataRepository
        extends JpaRepository<KeyMetadata, Long> {

    Optional<KeyMetadata> findByKeyIdAndStatus(
            String keyId,
            KeyStatus status
    );
    List<KeyMetadata> findByStatusAndExpiresAtLessThanEqual(
            KeyStatus status,
            LocalDateTime expiresAt
    );
}