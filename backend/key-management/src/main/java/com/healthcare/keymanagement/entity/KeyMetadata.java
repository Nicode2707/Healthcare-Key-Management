package com.healthcare.keymanagement.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "key_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeyMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String keyId;

    @Column(nullable = false)
    private String algorithm;

    @Column(nullable = false)
    private Integer keyVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KeyStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;

    @Column(nullable = false, length = 1000)
    private String protectedKey;
}