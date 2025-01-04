package com.devshaks.personal_finance.transactions;

import com.devshaks.personal_finance.budget.BudgetCategoryResponse;
import com.devshaks.personal_finance.budget.BudgetFeignClient;
import com.devshaks.personal_finance.budget.BudgetResponse;
import com.devshaks.personal_finance.exceptions.*;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.transaction.TransactionEventSender;
import com.devshaks.personal_finance.users.UserDetailsResponse;
import com.devshaks.personal_finance.users.UserFeignClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.devshaks.personal_finance.kafka.events.TransactionEvents.TRANSACTION_CREATED;
import static com.devshaks.personal_finance.kafka.events.TransactionEvents.TRANSACTION_FAILED_BUDGET_EXCEEDED;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionsService {
    private final TransactionsRepository transactionsRepository;
    private final TransactionsMapper transactionsMapper;
    private final UserFeignClient userFeignClient;
    private final BudgetFeignClient budgetFeignClient;
    private final AuditEventSender auditEventSender;
    private final TransactionEventSender transactionEventSender;

    @Transactional
    public TransactionsDTO newTransaction(@Valid TransactionsRequest transactionRequest, Long userId) {
        // Confirm the user exists by querying the User Service
        UserDetailsResponse user = userFeignClient.getUserProfileDetails(userId);
        if (user == null || user.userId() == null) {
            throw new UserNotFoundException("User not found");
        }

        // Retrieve all budgets associated with this user
        List<BudgetResponse> budgets = budgetFeignClient.getUserBudgets(userId);

        // If no budgets are found, approve the transaction by default
        if (budgets == null || budgets.isEmpty()) {
            Transactions transactions = transactionsMapper.toNewTransaction(transactionRequest);
            transactions.setUserId(userId);
            transactions.setTransactionStatus(TransactionsStatus.APPROVED); // Default status
            Transactions savedTransaction = transactionsRepository.save(transactions);

            // Send an audit event indicating the transaction was approved without a budget
            auditEventSender.sendEventToAudit(TRANSACTION_CREATED, userId, "Transaction Approved - No Budget Set");

            return transactionsMapper.toTransactionDTO(savedTransaction);
        }

        // Determine the budget for the current month
        YearMonth currentMonth = YearMonth.now();
        BudgetResponse currentMonthBudget = budgets.stream()
                .filter(b -> b.month().equals(currentMonth))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "No budget found for the current month"));

        // Check if the transaction amount exceeds the remaining budget
        BigDecimal transactionAmount = transactionRequest.amount();
        if (currentMonthBudget != null && currentMonthBudget.remainingAmount().compareTo(transactionAmount) < 0) {
            auditEventSender.sendEventToAudit(TRANSACTION_FAILED_BUDGET_EXCEEDED, userId,
                    "Failed Transaction, User Attempted to Breach Budget");
            throw new BudgetExceededException("Monthly Budget exceeded");
        }

        // Check if the transaction exceeds the category-specific budget (if applicable)
        String transactionCategory = transactionRequest.category();
        if (transactionCategory != null) {
            @SuppressWarnings("null")
            BudgetCategoryResponse category = currentMonthBudget.categories().stream()
                    .filter(cat -> cat.name().equalsIgnoreCase(transactionCategory))
                    .findFirst()
                    .orElse(null);

            if (category != null && category.categoryLimit() != null) {
                BigDecimal categoryRemaining = category.categoryLimit().subtract(category.spentAmount());
                if (categoryRemaining.compareTo(transactionAmount) < 0) {
                    auditEventSender.sendEventToAudit(TRANSACTION_FAILED_BUDGET_EXCEEDED, userId,
                            "Failed Transaction, User Attempted to Breach Budget");
                    throw new BudgetExceededException("Budget Limit Exceeded For Category");
                }
            }
        }

        // Create a new transaction and set its status to pending
        Transactions transactions = transactionsMapper.toNewTransaction(transactionRequest);
        transactions.setUserId(userId);
        transactions.setTransactionStatus(TransactionsStatus.PENDING);
        Transactions savedTransaction = transactionsRepository.save(transactions);

        // Ensure the transaction is persisted in the database
        transactionsRepository.flush();

        // Try to send the transaction event to the Budget Service
        try {
            transactionEventSender.sendEventToBudget(
                    transactions.getId(),
                    transactions.getUserId(),
                    transactions.getCategory(),
                    transactions.getAmount(),
                    transactions.getDescription());
        } catch (BudgetExceededException | BudgetNotFoundException ex) {
            // If budget validation fails, reject the transaction and log the reason
            savedTransaction.setTransactionStatus(TransactionsStatus.REJECTED);
            transactionsRepository.save(savedTransaction);
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        } catch (Exception ex) {
            // Handle unexpected errors, mark transaction as rejected
            savedTransaction.setTransactionStatus(TransactionsStatus.REJECTED);
            transactionsRepository.save(savedTransaction);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to validate transaction");
        }

        // Log and audit the successful creation of the transaction
        auditEventSender.sendEventToAudit(TRANSACTION_CREATED, userId, "New Transaction Created");
        return transactionsMapper.toTransactionDTO(savedTransaction);
    }

    /**
     * Retrieves all transactions for a specific user.
     * Caches the result for future lookups based on the userId.
     *
     * @param userId the ID of the user whose transactions are to be retrieved
     * @return a list of transactions mapped to TransactionsResponse
     * @throws TransactionNotFoundException if no transactions are found for the
     *                                      user
     */
    // @Cacheable(value = "transactions", key = "#userId")
    public List<TransactionsResponse> getUsersTransactions(Long userId) {
        List<Transactions> transactions = transactionsRepository.findByUserId(userId);
        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException("Cannot Find Transaction For this User");
        }
        return transactions.stream().map(transactionsMapper::mapUserToTransactionResponse).toList();
    }

    /**
     * Retrieves a specific transaction by its ID.
     * Caches the result for future lookups based on the transaction ID.
     *
     * @param id the ID of the transaction to be retrieved
     * @return a TransactionsDTO representing the transaction
     * @throws TransactionNotFoundException if no transaction is found with the
     *                                      given ID
     */
    // @Cacheable(value = "transactions", key = "#id")
    public TransactionsDTO getTransactionById(Long id) {
        return transactionsRepository.findById(id).map(transactionsMapper::toTransactionDTO)
                .orElseThrow(() -> new TransactionNotFoundException("Cannot Find Transaction With ID"));
    }

    /**
     * Retrieves transactions for a specific user filtered by category.
     * Caches the result for future lookups based on userId and category.
     *
     * @param userId   the ID of the user whose transactions are to be retrieved
     * @param category the category of transactions to filter by
     * @return a list of TransactionsDTO for the specified user and category
     * @throws TransactionNotFoundException if no transactions are found for the
     *                                      category
     */
    // @Cacheable(value = "transactions", key = "T(String).format('%d-%s', #userId,
    // #category)")
    public List<TransactionsDTO> getUserTransactionByCategory(Long userId, String category) {
        List<Transactions> transactions = transactionsRepository.findByUserIdAndCategory(userId, category);
        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException("Cannot Find Transaction For Category '" + category + "'");
        }
        return transactions.stream().map(transactionsMapper::toTransactionDTO).toList();
    }

    /**
     * Filters and paginates transactions based on various criteria.
     * Caches the result for future lookups based on userId, category, and pageable
     * parameters.
     *
     * @param userId             the ID of the user whose transactions are being
     *                           filtered
     * @param category           the category to filter by
     * @param transactionDate    the transaction date to filter by
     * @param transactionsType   the type of transactions to filter by
     * @param transactionsStatus the status of transactions to filter by
     * @param pageable           pagination details
     * @return a PaginatedTransactionDTO containing the filtered and paginated
     *         transactions
     */
    // @Cacheable(value = "transaction-filters", key = "#userId + '-' + #category +
    // '-' + #pageable.pageNumber")
    public PaginatedTransactionDTO getTransactionFilter(Long userId, String category, LocalDateTime transactionDate,
            TransactionsType transactionsType, TransactionsStatus transactionsStatus, Pageable pageable) {
        Specification<Transactions> specification = Specification.where(null);
        if (userId != null) {
            specification.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (category != null) {
            specification.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (transactionDate != null) {
            specification.and((root, query, cb) -> cb.equal(root.get("transactionDate"), transactionDate));
        }
        if (transactionsType != null) {
            specification.and((root, query, cb) -> cb.equal(root.get("transactionType"), transactionsType));
        }
        if (transactionsStatus != null) {
            specification = specification
                    .and((root, query, cb) -> cb.equal(root.get("transactionStatus"), transactionsStatus));
        }

        Page<Transactions> transactionsPage = transactionsRepository.findAll(specification, pageable);
        List<TransactionsDTO> content = transactionsPage.getContent().stream().map(transactionsMapper::toTransactionDTO)
                .collect(Collectors.toList());
        return new PaginatedTransactionDTO(content, transactionsPage.getNumber(), transactionsPage.getSize(),
                transactionsPage.getTotalElements(), transactionsPage.getTotalPages(), transactionsPage.isLast());
    }

    /**
     * Calculates statistics for a user's transactions, including daily, weekly, and
     * monthly spending and counts.
     *
     * @param userId the ID of the user whose transaction statistics are to be
     *               calculated
     * @return a UserTransactionStatisticsDTO containing transaction statistics and
     *         category breakdown
     */
    public UserTransactionStatisticsDTO getUserTransactionStats(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        List<Transactions> dailyTransactions = transactionsRepository.findByUserIdAndTransactionDateBetween(userId,
                startOfDay, now);
        List<Transactions> weeklyTransactions = transactionsRepository.findByUserIdAndTransactionDateBetween(userId,
                startOfWeek, now);
        List<Transactions> monthlyTransactions = transactionsRepository.findByUserIdAndTransactionDateBetween(userId,
                startOfMonth, now);

        double dailySpending = dailyTransactions.stream()
                .mapToDouble(transaction -> transaction.getAmount().doubleValue()).sum();
        int dailyCount = dailyTransactions.size();

        double weeklySpending = weeklyTransactions.stream()
                .mapToDouble(transaction -> transaction.getAmount().doubleValue()).sum();
        int weeklyCount = weeklyTransactions.size();

        double monthlySpending = monthlyTransactions.stream()
                .mapToDouble(transaction -> transaction.getAmount().doubleValue()).sum();
        int monthlyCount = monthlyTransactions.size();

        double averageDailySpending = dailySpending / (dailyCount == 0 ? 1 : dailySpending);
        double averageWeeklySpending = weeklySpending / 7;
        double averageMonthlySpending = monthlySpending / 30;

        Map<String, Double> categoryBreakdown = monthlyTransactions.stream()
                .collect(Collectors.groupingBy(Transactions::getCategory,
                        Collectors.summingDouble(transaction -> transaction.getAmount().doubleValue())));

        return new UserTransactionStatisticsDTO(dailySpending, dailyCount, weeklySpending, weeklyCount, monthlySpending,
                monthlyCount, averageDailySpending, averageWeeklySpending, averageMonthlySpending, categoryBreakdown);

    }

    /**
     * Deletes a transaction by its ID and evicts it from the cache.
     *
     * @param id the ID of the transaction to be deleted
     */
    @CacheEvict(value = "transactions", key = "#id")
    public void deleteTransaction(Long id) {

    }

}
