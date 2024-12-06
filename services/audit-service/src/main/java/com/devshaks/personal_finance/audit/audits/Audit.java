package com.devshaks.personal_finance.audit.audits;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collation = "audit_logs")
public class Audit {

    @Id
    private String id;
    private EventType eventType; // The type of Event
    private String serviceName; // The name of the service that generated the Event
    private Long userId; // Associated User ID.
    private String description; // Details about the Event.
    private LocalDateTime timestamp; // Timestamp of when the event occurred.
    private String ipAddress; // IP address associated with the event.
}
