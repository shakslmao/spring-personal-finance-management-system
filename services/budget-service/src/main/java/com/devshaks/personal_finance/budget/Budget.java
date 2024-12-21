package com.devshaks.personal_finance.budget;

import com.devshaks.personal_finance.budget.category.BudgetCategory;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "budgets")
@EntityListeners(AuditingEntityListener.class)
public class Budget {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal monthlyLimit;

    @Column(nullable = false)
    private BigDecimal spentAmount; // Total Amount Spent in a Current Month.

    @Column(nullable = false)
    private BigDecimal remainingAmount;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BudgetCategory> categories;

    @Column(nullable = false)
    private String month;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public YearMonth getMonthAsYearMonth() {
        return YearMonth.parse(this.month);
    }

    public void setMonthFromYearMonth(YearMonth yearMonth) {
        this.month = yearMonth.toString();
    }
}
