package com.healthcare.keymanagement.service;

import com.healthcare.keymanagement.entity.AuditLog;
import com.healthcare.keymanagement.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(
            String username,
            String action,
            String method,
            String endpoint,
            int status
    ) {

        AuditLog auditLog = new AuditLog(
                username,
                action,
                method,
                endpoint,
                status,
                LocalDateTime.now()
        );

        auditLogRepository.save(auditLog);
    }
}