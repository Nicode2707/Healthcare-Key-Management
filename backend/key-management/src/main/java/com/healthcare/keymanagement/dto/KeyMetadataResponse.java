package com.healthcare.keymanagement.dto;

import com.healthcare.keymanagement.entity.KeyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class KeyMetadataResponse {

    private Long id;

    private String keyId;

    private String algorithm;

    private Integer keyVersion;

    private KeyStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;
}