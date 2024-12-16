package com.devshaks.personal_finance.kafka.events;

import com.devshaks.personal_finance.kafka.budget.BudgetEvents;
import com.devshaks.personal_finance.kafka.notifications.NotificationEvents;
import com.devshaks.personal_finance.kafka.transactions.TransactionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventMapper {
    public Enum<?> mapEventToSpecificType(Enum<?> microServiceEvent) {
        if (microServiceEvent instanceof TransactionEvents) return (TransactionEvents) microServiceEvent;
        if (microServiceEvent instanceof NotificationEvents) return (NotificationEvents) microServiceEvent;
        if (microServiceEvent instanceof BudgetEvents) return (BudgetEvents) microServiceEvent;
        throw new IllegalArgumentException("Unsupported Microservice Event: " + microServiceEvent);
    }
}
