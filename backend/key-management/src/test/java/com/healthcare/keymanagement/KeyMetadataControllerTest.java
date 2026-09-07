package com.healthcare.keymanagement;

import com.healthcare.keymanagement.controller.KeyMetadataController;
import com.healthcare.keymanagement.dto.KeyMetadataResponse;
import com.healthcare.keymanagement.entity.KeyMetadata;
import com.healthcare.keymanagement.entity.KeyStatus;
import com.healthcare.keymanagement.service.KeyMetadataService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeyMetadataControllerTest {

    @Mock
    private KeyMetadataService service;

    @InjectMocks
    private KeyMetadataController controller;

    private KeyMetadata key;

    @BeforeEach
    void setUp() {

        key = KeyMetadata.builder()
                .id(1L)
                .keyId("CONTROLLER-TEST-001")
                .algorithm("AES-256")
                .keyVersion(1)
                .status(KeyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    void shouldGetAllKeys() {

        when(service.getAll())
                .thenReturn(List.of(key));

        List<KeyMetadataResponse> result =
                controller.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(
                "CONTROLLER-TEST-001",
                result.get(0).getKeyId()
        );

        assertEquals(
                "AES-256",
                result.get(0).getAlgorithm()
        );

        assertEquals(
                KeyStatus.ACTIVE,
                result.get(0).getStatus()
        );

        verify(service).getAll();
    }

    @Test
    void shouldRevokeKey() {

        key.setStatus(KeyStatus.REVOKED);

        when(service.revokeKey("CONTROLLER-TEST-001"))
                .thenReturn(key);

        KeyMetadataResponse result =
                controller.revokeKey(
                        "CONTROLLER-TEST-001"
                );

        assertNotNull(result);

        assertEquals(
                "CONTROLLER-TEST-001",
                result.getKeyId()
        );

        assertEquals(
                KeyStatus.REVOKED,
                result.getStatus()
        );

        verify(service)
                .revokeKey("CONTROLLER-TEST-001");
    }

    @Test
    void shouldRotateKey() {

        key.setKeyVersion(2);

        when(service.rotateKey("CONTROLLER-TEST-001"))
                .thenReturn(key);

        KeyMetadataResponse result =
                controller.rotateKey(
                        "CONTROLLER-TEST-001"
                );

        assertNotNull(result);

        assertEquals(
                2,
                result.getKeyVersion()
        );

        assertEquals(
                KeyStatus.ACTIVE,
                result.getStatus()
        );

        verify(service)
                .rotateKey("CONTROLLER-TEST-001");
    }
}