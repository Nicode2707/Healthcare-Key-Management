package com.healthcare.keymanagement.service;

import com.healthcare.keymanagement.dto.EmergencyRecoveryRequest;
import com.healthcare.keymanagement.entity.KeyMetadata;
import com.healthcare.keymanagement.entity.KeyRecoveryRequest;
import com.healthcare.keymanagement.entity.KeyStatus;
import com.healthcare.keymanagement.entity.RecoveryStatus;
import com.healthcare.keymanagement.exception.KeyNotFoundException;
import com.healthcare.keymanagement.repository.KeyMetadataRepository;
import com.healthcare.keymanagement.repository.KeyRecoveryRequestRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class KeyRecoveryService {

    private final KeyMetadataRepository keyMetadataRepository;
    private final KeyRecoveryRequestRepository recoveryRequestRepository;


    // ============================================================
    // 1. CREATE EMERGENCY RECOVERY REQUEST
    // ============================================================

    @Transactional
    public KeyRecoveryRequest createRecoveryRequest(
            String keyId,
            EmergencyRecoveryRequest request,
            String username
    ) {

        // --------------------------------------------------------
        // 1. Validate recovery reason
        // --------------------------------------------------------

        if (request == null
                || request.reason() == null
                || request.reason().isBlank()) {

            throw new IllegalArgumentException(
                    "Emergency recovery reason is required"
            );
        }


        // --------------------------------------------------------
        // 2. Verify that the key exists
        // --------------------------------------------------------

        var keys =
                keyMetadataRepository.findByKeyId(keyId);

        if (keys.isEmpty()) {

            throw new KeyNotFoundException(
                    "Key not found: " + keyId
            );
        }


        // --------------------------------------------------------
        // 3. Active keys must not enter emergency recovery
        // --------------------------------------------------------

        boolean activeKeyExists =
                keys.stream()
                        .anyMatch(key ->
                                key.getStatus() == KeyStatus.ACTIVE
                        );

        if (activeKeyExists) {

            throw new IllegalStateException(
                    "Active key does not require emergency recovery: "
                            + keyId
            );
        }


        // --------------------------------------------------------
        // 4. Prevent duplicate pending requests
        // --------------------------------------------------------

        boolean pendingRequestExists =
                !recoveryRequestRepository
                        .findByKeyIdAndStatus(
                                keyId,
                                RecoveryStatus.PENDING
                        )
                        .isEmpty();

        if (pendingRequestExists) {

            throw new IllegalStateException(
                    "A recovery request is already pending for key: "
                            + keyId
            );
        }


        // --------------------------------------------------------
        // 5. Create recovery request
        // --------------------------------------------------------

        KeyRecoveryRequest recoveryRequest =
                new KeyRecoveryRequest();

        recoveryRequest.setKeyId(keyId);

        recoveryRequest.setRequestedBy(username);

        recoveryRequest.setReason(
                request.reason().trim()
        );

        recoveryRequest.setStatus(
                RecoveryStatus.PENDING
        );

        recoveryRequest.setRequestedAt(
                LocalDateTime.now()
        );

        return recoveryRequestRepository.save(
                recoveryRequest
        );
    }


    // ============================================================
    // 2. APPROVE EMERGENCY RECOVERY REQUEST
    // ============================================================

    @Transactional
    public KeyRecoveryRequest approveRecoveryRequest(
            Long requestId,
            String adminUsername
    ) {

        // --------------------------------------------------------
        // 1. Find recovery request
        // --------------------------------------------------------

        KeyRecoveryRequest recoveryRequest =
                recoveryRequestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Recovery request not found: "
                                                + requestId
                                )
                        );


        // --------------------------------------------------------
        // 2. Request must currently be PENDING
        // --------------------------------------------------------

        if (recoveryRequest.getStatus()
                != RecoveryStatus.PENDING) {

            throw new IllegalStateException(
                    "Recovery request is not pending: "
                            + requestId
            );
        }


        // --------------------------------------------------------
        // 3. Verify associated key
        // --------------------------------------------------------

        var keys =
                keyMetadataRepository.findByKeyId(
                        recoveryRequest.getKeyId()
                );

        if (keys.isEmpty()) {

            throw new IllegalArgumentException(
                    "Associated key not found: "
                            + recoveryRequest.getKeyId()
            );
        }


        // --------------------------------------------------------
        // 4. ACTIVE key must not enter emergency recovery
        // --------------------------------------------------------

        boolean activeKeyExists =
                keys.stream()
                        .anyMatch(key ->
                                key.getStatus() == KeyStatus.ACTIVE
                        );

        if (activeKeyExists) {

            throw new IllegalStateException(
                    "Active key does not require emergency recovery: "
                            + recoveryRequest.getKeyId()
            );
        }


        // --------------------------------------------------------
        // 5. Approve request
        // --------------------------------------------------------

        recoveryRequest.setStatus(
                RecoveryStatus.APPROVED
        );

        recoveryRequest.setProcessedAt(
                LocalDateTime.now()
        );

        return recoveryRequestRepository.save(
                recoveryRequest
        );
    }


    // ============================================================
    // 3. EXECUTE EMERGENCY RECOVERY
    // ============================================================

    @Transactional
    public KeyRecoveryRequest executeRecovery(
            Long requestId,
            String adminUsername
    ) {

        // --------------------------------------------------------
        // 1. Find recovery request
        // --------------------------------------------------------

        KeyRecoveryRequest recoveryRequest =
                recoveryRequestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Recovery request not found: "
                                                + requestId
                                )
                        );


        // --------------------------------------------------------
        // 2. Recovery must be APPROVED first
        // --------------------------------------------------------

        if (recoveryRequest.getStatus()
                != RecoveryStatus.APPROVED) {

            throw new IllegalStateException(
                    "Recovery request must be APPROVED before execution: "
                            + requestId
            );
        }


        // --------------------------------------------------------
        // 3. Load all versions of the key
        // --------------------------------------------------------

        var keys =
                keyMetadataRepository.findByKeyId(
                        recoveryRequest.getKeyId()
                );

        if (keys.isEmpty()) {

            throw new KeyNotFoundException(
                    "Associated key not found: "
                            + recoveryRequest.getKeyId()
            );
        }


        // --------------------------------------------------------
        // 4. Prevent duplicate active key
        // --------------------------------------------------------

        boolean activeKeyExists =
                keys.stream()
                        .anyMatch(key ->
                                key.getStatus() == KeyStatus.ACTIVE
                        );

        if (activeKeyExists) {

            throw new IllegalStateException(
                    "An active key already exists for: "
                            + recoveryRequest.getKeyId()
            );
        }


        // --------------------------------------------------------
        // 5. Find the latest recoverable ARCHIVED version
        // --------------------------------------------------------

        LocalDateTime now =
                LocalDateTime.now();

        KeyMetadata recoverableKey =
                keys.stream()
                        .filter(key ->
                                key.getStatus()
                                        == KeyStatus.ARCHIVED
                        )
                        .filter(key ->
                                key.getExpiresAt() == null
                                        || key.getExpiresAt()
                                        .isAfter(now)
                        )
                        .max(
                                Comparator.comparing(
                                        KeyMetadata::getKeyVersion
                                )
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No recoverable archived key found for: "
                                                + recoveryRequest.getKeyId()
                                )
                        );


        // --------------------------------------------------------
        // 6. Restore archived key
        // --------------------------------------------------------

        recoverableKey.setStatus(
                KeyStatus.ACTIVE
        );

        /*
         * A recovered key is no longer revoked.
         */
        recoverableKey.setRevokedAt(null);

        keyMetadataRepository.save(
                recoverableKey
        );


        // --------------------------------------------------------
        // 7. Mark recovery request COMPLETED
        // --------------------------------------------------------

        recoveryRequest.setStatus(
                RecoveryStatus.COMPLETED
        );

        recoveryRequest.setProcessedAt(
                LocalDateTime.now()
        );

        return recoveryRequestRepository.save(
                recoveryRequest
        );
    }
}