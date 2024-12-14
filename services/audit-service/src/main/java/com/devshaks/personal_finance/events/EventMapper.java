package com.devshaks.personal_finance.events;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventMapper {

    public Enum<?> mapEventToSpecificType(Enum<?> microServiceEvent) {
        if (microServiceEvent instanceof UserEvents) return (UserEvents) microServiceEvent;
        if (microServiceEvent instanceof TransactionEvents) return (TransactionEvents) microServiceEvent;
        if (microServiceEvent instanceof BudgetEvents) return (BudgetEvents) microServiceEvent;
        if (microServiceEvent instanceof AuditEvents) return (AuditEvents) microServiceEvent;
        throw new IllegalArgumentException("Unsupported Microservice Event: " + microServiceEvent);
    }
}
