package com.devshaks.personal_finance.audit.audits;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface AuditRepository extends MongoRepository<AuditLog, String> {
    Page<AuditLog> findByUserId(String userId, Pageable pageable);
    Page<AuditLog> findByEventType(String eventType, Pageable pageable);
    Page<AuditLog> findByServiceName(String serviceName, Pageable pageable);
}
