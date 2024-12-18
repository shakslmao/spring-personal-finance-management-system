package com.devshaks.personal_finance.transactions;

import java.util.Map;

public record UserTransactionStatisticsDTO(
        double dailySpending,
        int dailyTransactionCount,

        double weeklySpending,
        int weeklySpendingCount,

        double monthlySpending,
        int monthlySpendingCount,

        double averageDailySpending,
        double averageWeeklySpending,
        double averageMonthlySpending,

        Map<String, Double> categoryBreakdown
) {
}
