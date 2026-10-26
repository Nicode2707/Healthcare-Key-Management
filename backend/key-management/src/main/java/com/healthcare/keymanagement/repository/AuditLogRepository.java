package com.healthcare.keymanagement.repository;

import com.healthcare.keymanagement.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Return the newest audit logs first.
    List<AuditLog> findAllByOrderByTimestampDesc();
}

