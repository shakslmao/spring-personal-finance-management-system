package com.devshaks.personal_finance.audits;

import com.devshaks.personal_finance.events.EventType;
import com.devshaks.personal_finance.events.UserEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AuditCustomRepository {
    private final MongoTemplate mongoTemplate;

    public Page<Audit> findAll(String serviceName, Long userId, Enum<?> eventType, Pageable pageable) {
        Criteria criteria = new Criteria();

        if (serviceName != null) {
            criteria.and("serviceName").is(serviceName);
        }

        if (userId != null) {
            criteria.and("userId").is(userId);
        }

        if (eventType != null) {
            criteria.and("eventType").is(eventType);
        }

        Query query = new Query(criteria).with(pageable);
        List<Audit> audit = mongoTemplate.find(query, Audit.class);
        long count = mongoTemplate.count(query.skip(0).limit(0), Audit.class);
        return new PageImpl<>(audit, pageable, count);
    }
}
