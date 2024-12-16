package com.devshaks.personal_finance.transactions;

import com.devshaks.personal_finance.kafka.services.ServiceNames;
import com.devshaks.personal_finance.users.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Transactions {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private ServiceNames serviceName;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionsType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionsStatus transactionStatus;

    @Column(nullable = false)
    private String description;
}
