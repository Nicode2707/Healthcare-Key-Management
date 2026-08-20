package com.healthcare.keymanagement.repository;

import com.healthcare.keymanagement.entity.KeyMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeyMetadataRepository
        extends JpaRepository<KeyMetadata, Long> {

    Optional<KeyMetadata> findByKeyId(String keyId);
}