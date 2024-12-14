package com.devshaks.personal_finance.kafka;

import com.devshaks.personal_finance.events.UserEvents;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserEventDTO(
        @JsonProperty("eventType") UserEvents eventType,
        @JsonProperty("serviceName") ServiceNames serviceName,
        @JsonProperty("userId") Long userId,
        @JsonProperty("description") String description,
        @JsonProperty("timestamp") String timestamp
) {

}
