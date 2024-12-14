package com.devshaks.personal_finance.audits;

import com.devshaks.personal_finance.events.EventType;
import com.devshaks.personal_finance.events.UserEvents;
import com.devshaks.personal_finance.kafka.ServiceNames;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface AuditRepository extends MongoRepository<Audit, String> {
    Page<Audit> findByUserId(Long userId, Pageable pageable);
    Page<Audit> findByEventType(Enum<?> eventType, Pageable pageable);
    Page<Audit> findByServiceName(ServiceNames serviceName, Pageable pageable);
}
