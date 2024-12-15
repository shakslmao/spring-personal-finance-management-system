package com.devshaks.personal_finance.audits;

import com.devshaks.personal_finance.kafka.services.ServiceNames;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditRepository auditRepository;
    private final AuditCustomRepository auditCustomRepository;
    private final AuditMapper auditMapper;

    public List<AuditDTO> getAllAuditLogs() {
        return auditRepository.findAll()
                .stream()
                .map(auditMapper::toAuditDTO)
                .collect(Collectors.toList());
    }

    public List<AuditDTO> getUserAuditLogs(Long userId, Pageable pageable) {
        return auditRepository.findByUserId(userId, pageable)
                .getContent()
                .stream()
                .map(auditMapper::toAuditDTO)
                .collect(Collectors.toList());
    }

    public List<AuditDTO> getEventAuditLogs(Enum<?> eventType, Pageable pageable) {
        return auditRepository.findByEventType(eventType, pageable)
                .getContent()
                .stream()
                .map(auditMapper::toAuditDTO)
                .collect(Collectors.toList());
    }

    public List<AuditDTO> getServiceAuditLogs(ServiceNames serviceName, Pageable pageable) {
        return auditRepository.findByServiceName(serviceName, pageable)
                .getContent()
                .stream()
                .map(auditMapper::toAuditDTO)
                .collect(Collectors.toList());
    }

    public List<AuditDTO> searchAuditLogs(Long userId, Enum<?> eventType, String serviceName, Pageable pageable) {
        return auditCustomRepository.findAll(serviceName, userId, eventType, pageable)
                .getContent()
                .stream()
                .map(auditMapper::toAuditDTO)
                .collect(Collectors.toList());
    }
}
