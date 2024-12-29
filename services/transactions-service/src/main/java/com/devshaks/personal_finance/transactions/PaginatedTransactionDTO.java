package com.devshaks.personal_finance.transactions;

import java.util.List;

public record PaginatedTransactionDTO(
        List<TransactionsDTO> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isLastPage
) {
}
