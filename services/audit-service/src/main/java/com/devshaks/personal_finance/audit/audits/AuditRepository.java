package com.devshaks.personal_finance.audit.audits;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface AuditRepository extends MongoRepository<Audit, String> {
    Page<Audit> findByUserId(Long userId, Pageable pageable);
    Page<Audit> findByEventType(EventType eventType, Pageable pageable);
    Page<Audit> findByServiceName(String serviceName, Pageable pageable);
}
