package com.healthcare.keymanagement.repository;

import com.healthcare.keymanagement.entity.KeyRecoveryRequest;
import com.healthcare.keymanagement.entity.RecoveryStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeyRecoveryRequestRepository
        extends JpaRepository<KeyRecoveryRequest, Long> {

    List<KeyRecoveryRequest> findByKeyId(String keyId);

    List<KeyRecoveryRequest> findByKeyIdAndStatus(
            String keyId,
            RecoveryStatus status
    );
}