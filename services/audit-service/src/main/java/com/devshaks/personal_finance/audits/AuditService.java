package com.devshaks.personal_finance.audits;

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

    /**
     *
     * @return
     */
    public List<AuditDTO> getAllAuditLogs() {
        return auditRepository.findAll()
                .stream()
                .map(auditMapper::toAuditDTO)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param userId
     * @param pageable
     * @return
     */
    public List<AuditDTO> getUserAuditLogs(Long userId, Pageable pageable) {
        return auditRepository.findByUserId(userId, pageable)
                .getContent()
                .stream()
                .map(auditMapper::toAuditDTO)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param eventType
     * @param pageable
     * @return
     */
    public List<AuditDTO> getEventAuditLogs(EventType eventType, Pageable pageable) {
        return auditRepository.findByEventType(eventType, pageable)
                .getContent()
                .stream()
                .map(auditMapper::toAuditDTO)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param serviceName
     * @return
     */
    public List<AuditDTO> getServiceAuditLogs(String serviceName, Pageable pageable) {
        return auditRepository.findByServiceName(serviceName, pageable)
                .getContent()
                .stream()
                .map(auditMapper::toAuditDTO)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param userId
     * @param eventType
     * @param serviceName
     * @return
     */
    public List<AuditDTO> searchAuditLogs(Long userId, EventType eventType, String serviceName, Pageable pageable) {
        return auditCustomRepository.findAll(serviceName, userId, eventType, pageable)
                .getContent()
                .stream()
                .map(auditMapper::toAuditDTO)
                .collect(Collectors.toList());
    }
}
