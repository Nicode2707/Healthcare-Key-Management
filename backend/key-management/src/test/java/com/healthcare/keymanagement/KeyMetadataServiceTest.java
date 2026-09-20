package com.healthcare.keymanagement;

import com.healthcare.keymanagement.entity.KeyMetadata;
import com.healthcare.keymanagement.entity.KeyStatus;
import com.healthcare.keymanagement.repository.KeyMetadataRepository;
import com.healthcare.keymanagement.service.AesKeyService;
import com.healthcare.keymanagement.service.BlockchainService;
import com.healthcare.keymanagement.service.KeyMetadataService;
import com.healthcare.keymanagement.service.KeyProtectionService;
import com.healthcare.keymanagement.service.LifecycleIntegrityService;

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

    // =========================================================
    // MOCK DEPENDENCIES
    // =========================================================

    @Mock
    private KeyMetadataRepository repository;

    @Mock
    private AesKeyService aesKeyService;

    @Mock
    private KeyProtectionService keyProtectionService;

    @Mock
    private LifecycleIntegrityService lifecycleIntegrityService;

    @Mock
    private BlockchainService blockchainService;

    @InjectMocks
    private KeyMetadataService service;

    private KeyMetadata activeKey;


    // =========================================================
    // SETUP
    // =========================================================

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


    // =========================================================
    // CREATE KEY
    // =========================================================

    @Test
    void shouldCreateKeySuccessfully() {

        when(aesKeyService.generateAes256Key())
                .thenReturn("RAW-AES-256-KEY");

        when(keyProtectionService.protectKey("RAW-AES-256-KEY"))
                .thenReturn("PROTECTED-KEY");

        when(lifecycleIntegrityService.generateRecordHash(
                "TEST-KEY-001",
                1,
                "CREATED"
        )).thenReturn("TEST-CREATED-HASH");

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
                .expiresAt(
                        LocalDateTime.now().plusDays(30)
                )
                .build();

        KeyMetadata result =
                service.create(request);


        // ---------------------------------------------------------
        // Verify result
        // ---------------------------------------------------------

        assertNotNull(result);

        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "TEST-KEY-001",
                result.getKeyId()
        );

        assertEquals(
                "AES-256",
                result.getAlgorithm()
        );

        assertEquals(
                1,
                result.getKeyVersion()
        );

        assertEquals(
                KeyStatus.ACTIVE,
                result.getStatus()
        );

        assertNotNull(
                result.getCreatedAt()
        );

        assertEquals(
                "PROTECTED-KEY",
                result.getProtectedKey()
        );


        // ---------------------------------------------------------
        // Verify AES key generation
        // ---------------------------------------------------------

        verify(aesKeyService)
                .generateAes256Key();


        // ---------------------------------------------------------
        // Verify key protection
        // ---------------------------------------------------------

        verify(keyProtectionService)
                .protectKey(
                        "RAW-AES-256-KEY"
                );


        // ---------------------------------------------------------
        // Verify lifecycle hash
        // ---------------------------------------------------------

        verify(lifecycleIntegrityService)
                .generateRecordHash(
                        "TEST-KEY-001",
                        1,
                        "CREATED"
                );


        // ---------------------------------------------------------
        // Verify database save
        // ---------------------------------------------------------

        verify(repository)
                .save(any(KeyMetadata.class));


        // ---------------------------------------------------------
        // Verify blockchain recording
        // ---------------------------------------------------------

        verify(blockchainService)
                .recordKeyLifecycleEvent(
                        "TEST-KEY-001",
                        1,
                        0,
                        "TEST-CREATED-HASH"
                );
    }


    // =========================================================
    // REVOKE KEY
    // =========================================================

    @Test
    void shouldRevokeActiveKeySuccessfully() {

        when(repository.findByKeyIdAndStatus(
                "TEST-KEY-001",
                KeyStatus.ACTIVE
        )).thenReturn(
                Optional.of(activeKey)
        );

        when(repository.save(activeKey))
                .thenReturn(activeKey);

        when(lifecycleIntegrityService.generateRecordHash(
                "TEST-KEY-001",
                1,
                "REVOKED"
        )).thenReturn("TEST-REVOKED-HASH");


        KeyMetadata result =
                service.revokeKey(
                        "TEST-KEY-001"
                );


        // ---------------------------------------------------------
        // Verify result
        // ---------------------------------------------------------

        assertNotNull(result);

        assertEquals(
                KeyStatus.REVOKED,
                result.getStatus()
        );

        assertNotNull(
                result.getRevokedAt()
        );


        // ---------------------------------------------------------
        // Verify database
        // ---------------------------------------------------------

        verify(repository)
                .save(activeKey);


        // ---------------------------------------------------------
        // Verify lifecycle hash
        // ---------------------------------------------------------

        verify(lifecycleIntegrityService)
                .generateRecordHash(
                        "TEST-KEY-001",
                        1,
                        "REVOKED"
                );


        // ---------------------------------------------------------
        // Verify blockchain recording
        // ---------------------------------------------------------

        verify(blockchainService)
                .recordKeyLifecycleEvent(
                        "TEST-KEY-001",
                        1,
                        2,
                        "TEST-REVOKED-HASH"
                );
    }


    // =========================================================
    // REVOKE MISSING KEY
    // =========================================================

    @Test
    void shouldThrowExceptionWhenRevokingMissingActiveKey() {

        when(repository.findByKeyIdAndStatus(
                "MISSING-KEY",
                KeyStatus.ACTIVE
        )).thenReturn(
                Optional.empty()
        );


        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> service.revokeKey(
                                "MISSING-KEY"
                        )
                );


        assertTrue(
                exception.getMessage()
                        .contains(
                                "Active key not found"
                        )
        );


        // ---------------------------------------------------------
        // No database update
        // ---------------------------------------------------------

        verify(
                repository,
                never()
        ).save(
                any(KeyMetadata.class)
        );


        // ---------------------------------------------------------
        // No blockchain activity
        // ---------------------------------------------------------

        verify(
                lifecycleIntegrityService,
                never()
        ).generateRecordHash(
                anyString(),
                anyInt(),
                anyString()
        );

        verify(
                blockchainService,
                never()
        ).recordKeyLifecycleEvent(
                anyString(),
                anyInt(),
                anyInt(),
                anyString()
        );
    }


    // =========================================================
    // ROTATE KEY
    // =========================================================

    @Test
    void shouldRotateActiveKeySuccessfully() {

        when(repository.findByKeyIdAndStatus(
                "TEST-KEY-001",
                KeyStatus.ACTIVE
        )).thenReturn(
                Optional.of(activeKey)
        );


        when(aesKeyService.generateAes256Key())
                .thenReturn(
                        "NEW-RAW-AES-KEY"
                );


        when(keyProtectionService.protectKey(
                "NEW-RAW-AES-KEY"
        )).thenReturn(
                "NEW-PROTECTED-KEY"
        );


        when(repository.save(
                any(KeyMetadata.class)
        )).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        when(lifecycleIntegrityService.generateRecordHash(
                "TEST-KEY-001",
                2,
                "ROTATED"
        )).thenReturn(
                "TEST-ROTATED-HASH"
        );


        KeyMetadata result =
                service.rotateKey(
                        "TEST-KEY-001"
                );


        // ---------------------------------------------------------
        // Verify result
        // ---------------------------------------------------------

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


        // ---------------------------------------------------------
        // Verify AES generation
        // ---------------------------------------------------------

        verify(aesKeyService)
                .generateAes256Key();


        // ---------------------------------------------------------
        // Verify protection
        // ---------------------------------------------------------

        verify(keyProtectionService)
                .protectKey(
                        "NEW-RAW-AES-KEY"
                );


        // ---------------------------------------------------------
        // Verify lifecycle hash
        // ---------------------------------------------------------

        verify(lifecycleIntegrityService)
                .generateRecordHash(
                        "TEST-KEY-001",
                        2,
                        "ROTATED"
                );


        // ---------------------------------------------------------
        // Verify database saves
        // ---------------------------------------------------------

        verify(
                repository,
                times(2)
        ).save(
                any(KeyMetadata.class)
        );


        // ---------------------------------------------------------
        // Verify blockchain recording
        // ---------------------------------------------------------

        verify(blockchainService)
                .recordKeyLifecycleEvent(
                        "TEST-KEY-001",
                        2,
                        1,
                        "TEST-ROTATED-HASH"
                );
    }


    // =========================================================
    // ROTATE MISSING KEY
    // =========================================================

    @Test
    void shouldNotRotateMissingActiveKey() {

        when(repository.findByKeyIdAndStatus(
                "MISSING-KEY",
                KeyStatus.ACTIVE
        )).thenReturn(
                Optional.empty()
        );


        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> service.rotateKey(
                                "MISSING-KEY"
                        )
                );


        assertTrue(
                exception.getMessage()
                        .contains(
                                "Active key not found"
                        )
        );


        // ---------------------------------------------------------
        // AES must not be generated
        // ---------------------------------------------------------

        verify(
                aesKeyService,
                never()
        ).generateAes256Key();


        // ---------------------------------------------------------
        // Blockchain must not be touched
        // ---------------------------------------------------------

        verify(
                lifecycleIntegrityService,
                never()
        ).generateRecordHash(
                anyString(),
                anyInt(),
                anyString()
        );

        verify(
                blockchainService,
                never()
        ).recordKeyLifecycleEvent(
                anyString(),
                anyInt(),
                anyInt(),
                anyString()
        );
    }
}