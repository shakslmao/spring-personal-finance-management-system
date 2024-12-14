package com.devshaks.personal_finance.audits;

import com.devshaks.personal_finance.events.EventType;
import com.devshaks.personal_finance.kafka.ServiceNames;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/audits")
@Tag(name = "Audit Controller", description = "")
public class AuditController {
    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Retrieves all Audit Logs")
    public ResponseEntity<List<AuditDTO>> getAllAuditLogs() {
        return ResponseEntity.ok(auditService.getAllAuditLogs());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Retrieves All Audit Logs for User")
    public ResponseEntity<List<AuditDTO>> getUserAuditLogs(@PathVariable("userId") Long userId, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(auditService.getUserAuditLogs(userId, pageable));
    }

    @GetMapping("/event/{eventType}")
    @Operation(summary = "Retrieves All Event Audit Logs")
    public ResponseEntity<List<AuditDTO>> getEventAuditLogs(@PathVariable("eventType") Enum<?> eventType, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(auditService.getEventAuditLogs(eventType, pageable));
    }

    @GetMapping("/event/{serviceName}")
    @Operation(summary = "Retrieves All Logs for the Service")
    public ResponseEntity<List<AuditDTO>> getServiceAuditLogs(@PathVariable("serviceName") ServiceNames serviceName, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(auditService.getServiceAuditLogs(serviceName, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search audit logs based on filters")
    public ResponseEntity<List<AuditDTO>> searchAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) String serviceName,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(auditService.searchAuditLogs(userId, eventType, serviceName, pageable));
    }
}
