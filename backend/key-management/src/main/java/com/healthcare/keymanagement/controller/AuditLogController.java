package com.healthcare.keymanagement.controller;

import com.healthcare.keymanagement.entity.AuditLog;
import com.healthcare.keymanagement.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    /**
     * Returns audit logs in newest-first order.
     * Only users with the ADMIN role can access this endpoint.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> getAuditLogs() {

        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
