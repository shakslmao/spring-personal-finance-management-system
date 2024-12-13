package com.devshaks.personal_finance.audits;

import com.devshaks.personal_finance.kafka.ServiceNames;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private ServiceNames serviceName; // The name of the service that generated the Event
    private Long userId; // Associated User ID.
    private String description; // Details about the Event.
    private String timestamp; // Timestamp of when the event occurred.
    private String ipAddress; // IP address associated with the event.
}
