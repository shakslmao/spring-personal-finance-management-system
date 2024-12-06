package com.devshaks.personal_finance.audit.audits;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    public List<AuditDTO> getUserAuditLogs(String userId, Pageable pageable) {
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
    public List<AuditDTO> getEventAuditLogs(String eventType, Pageable pageable) {
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
    public List<AuditDTO> searchAuditLogs(String userId, String eventType, String serviceName, Pageable pageable) {
      return auditCustomRepository.findAll(userId, serviceName, eventType, pageable)
              .getContent()
              .stream()
              .map(auditMapper::toAuditDTO)
              .collect(Collectors.toList());
    }
}
