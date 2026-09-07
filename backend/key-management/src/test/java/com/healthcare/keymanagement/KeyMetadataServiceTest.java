package com.healthcare.keymanagement;

import com.healthcare.keymanagement.entity.KeyMetadata;
import com.healthcare.keymanagement.entity.KeyStatus;
import com.healthcare.keymanagement.repository.KeyMetadataRepository;
import com.healthcare.keymanagement.service.AesKeyService;
import com.healthcare.keymanagement.service.KeyMetadataService;
import com.healthcare.keymanagement.service.KeyProtectionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeyMetadataServiceTest {

    @Mock
    private KeyMetadataRepository repository;

    @Mock
    private AesKeyService aesKeyService;

    @Mock
    private KeyProtectionService keyProtectionService;

    @InjectMocks
    private KeyMetadataService service;

    private KeyMetadata activeKey;

    @BeforeEach
    void setUp() {

        activeKey = KeyMetadata.builder()
                .id(1L)
                .keyId("TEST-KEY-001")
                .algorithm("AES-256")
                .keyVersion(1)
                .status(KeyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    void shouldCreateKeySuccessfully() {

        when(aesKeyService.generateAes256Key())
                .thenReturn("RAW-AES-256-KEY");

        when(keyProtectionService.protectKey("RAW-AES-256-KEY"))
                .thenReturn("PROTECTED-KEY");

        when(repository.save(any(KeyMetadata.class)))
                .thenAnswer(invocation -> {

                    KeyMetadata key =
                            invocation.getArgument(0);

                    key.setId(1L);

                    return key;
                });

        KeyMetadata request = KeyMetadata.builder()
                .keyId("TEST-KEY-001")
                .algorithm("AES-256")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        KeyMetadata result =
                service.create(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TEST-KEY-001", result.getKeyId());
        assertEquals("AES-256", result.getAlgorithm());
        assertEquals(1, result.getKeyVersion());
        assertEquals(KeyStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertEquals("PROTECTED-KEY", result.getProtectedKey());

        verify(aesKeyService).generateAes256Key();
        verify(keyProtectionService)
                .protectKey("RAW-AES-256-KEY");
        verify(repository).save(any(KeyMetadata.class));
    }

    @Test
    void shouldRevokeActiveKeySuccessfully() {

        when(repository.findByKeyIdAndStatus(
                "TEST-KEY-001",
                KeyStatus.ACTIVE
        )).thenReturn(Optional.of(activeKey));

        when(repository.save(activeKey))
                .thenReturn(activeKey);

        KeyMetadata result =
                service.revokeKey("TEST-KEY-001");

        assertEquals(KeyStatus.REVOKED, result.getStatus());
        assertNotNull(result.getRevokedAt());

        verify(repository).save(activeKey);
    }

    @Test
    void shouldThrowExceptionWhenRevokingMissingActiveKey() {

        when(repository.findByKeyIdAndStatus(
                "MISSING-KEY",
                KeyStatus.ACTIVE
        )).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> service.revokeKey("MISSING-KEY")
                );

        assertTrue(
                exception.getMessage()
                        .contains("Active key not found")
        );

        verify(repository, never())
                .save(any(KeyMetadata.class));
    }

    @Test
    void shouldRotateActiveKeySuccessfully() {

        when(repository.findByKeyIdAndStatus(
                "TEST-KEY-001",
                KeyStatus.ACTIVE
        )).thenReturn(Optional.of(activeKey));

        when(aesKeyService.generateAes256Key())
                .thenReturn("NEW-RAW-AES-KEY");

        when(keyProtectionService.protectKey(
                "NEW-RAW-AES-KEY"
        )).thenReturn("NEW-PROTECTED-KEY");

        when(repository.save(any(KeyMetadata.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        KeyMetadata result =
                service.rotateKey("TEST-KEY-001");

        assertNotNull(result);

        assertEquals(
                KeyStatus.ROTATED,
                activeKey.getStatus()
        );

        assertEquals(
                KeyStatus.ACTIVE,
                result.getStatus()
        );

        assertEquals(
                2,
                result.getKeyVersion()
        );

        assertEquals(
                "TEST-KEY-001",
                result.getKeyId()
        );

        assertEquals(
                "NEW-PROTECTED-KEY",
                result.getProtectedKey()
        );

        verify(aesKeyService)
                .generateAes256Key();

        verify(keyProtectionService)
                .protectKey("NEW-RAW-AES-KEY");

        verify(repository, times(2))
                .save(any(KeyMetadata.class));
    }

    @Test
    void shouldNotRotateMissingActiveKey() {

        when(repository.findByKeyIdAndStatus(
                "MISSING-KEY",
                KeyStatus.ACTIVE
        )).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> service.rotateKey("MISSING-KEY")
                );

        assertTrue(
                exception.getMessage()
                        .contains("Active key not found")
        );

        verify(aesKeyService, never())
                .generateAes256Key();
    }
}