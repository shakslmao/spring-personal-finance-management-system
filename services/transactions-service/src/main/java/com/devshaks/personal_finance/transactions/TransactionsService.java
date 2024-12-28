package com.devshaks.personal_finance.transactions;

import com.devshaks.personal_finance.budget.BudgetCategoryResponse;
import com.devshaks.personal_finance.budget.BudgetFeignClient;
import com.devshaks.personal_finance.budget.BudgetResponse;
import com.devshaks.personal_finance.exceptions.*;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.payment.PaymentEventSender;
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
    private final PaymentEventSender paymentEventSender;

    public TransactionsDTO newTransaction(@Valid TransactionsRequest transactionRequest, Long userId) {
        // Confirm user exists
        UserDetailsResponse user = userFeignClient.getUserProfileDetails(userId);
        if (user == null || user.userId() == null) {
            throw new UserNotFoundException("User not found");
        }

        // Get all budgets for this user
        List<BudgetResponse> budgets = budgetFeignClient.getUserBudgets(userId);
        if (budgets == null || budgets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No budgets found for user");
        }

        // find the current month budget
        YearMonth currentMonth = YearMonth.now();
        BudgetResponse currentMonthBudget = budgets.stream()
                .filter(b -> b.month().equals(currentMonth))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "No budget found for the current month"));

        // check if the transaction amount exceeds remaining budget
        BigDecimal transactionAmount = transactionRequest.amount();
        if (currentMonthBudget != null && currentMonthBudget.remainingAmount().compareTo(transactionAmount) < 0) {
            auditEventSender.sendEventToAudit(TRANSACTION_FAILED_BUDGET_EXCEEDED, userId,
                    "Failed Transaction, User Attempted to Breach Budget");
            throw new BudgetExceededException("Monthly Budget exceeded");
        }

        String transactionCategory = transactionRequest.category();
        if (transactionCategory != null) {
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

        Transactions transactions = transactionsMapper.toNewTransaction(transactionRequest);
        transactions.setUserId(userId);
        transactions.setPaymentStatus(PaymentStatus.PAYMENT_PENDING);
        transactions.setTransactionStatus(TransactionsStatus.PENDING);
        Transactions savedTransaction = transactionsRepository.save(transactions);

        try {
            transactionEventSender.sendEventToBudget(
                    transactions.getId(),
                    transactions.getUserId(),
                    transactions.getCategory(),
                    transactions.getAmount(),
                    transactions.getDescription());
        } catch (BudgetExceededException | BudgetNotFoundException ex) {
            savedTransaction.setTransactionStatus(TransactionsStatus.REJECTED);
            transactionsRepository.save(savedTransaction);
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        } catch (Exception ex) {
            savedTransaction.setTransactionStatus(TransactionsStatus.REJECTED);
            transactionsRepository.save(savedTransaction);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to validate transaction");
        }

        try {
            paymentEventSender.sendEventToPayment(
                    transactions.getUserId(),
                    transactions.getId(),
                    transactions.getAmount());

        } catch (PaymentValidationException ex) {
            savedTransaction.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);
            transactionsRepository.save(savedTransaction);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send payment validation event");
        }

        auditEventSender.sendEventToAudit(TRANSACTION_CREATED, userId, "New Transaction Created");
        return transactionsMapper.toTransactionDTO(savedTransaction);
    }

    // @Cacheable(value = "transactions", key = "#userId")
    public List<TransactionsResponse> getUsersTransactions(Long userId) {
        List<Transactions> transactions = transactionsRepository.findByUserId(userId);
        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException("Cannot Find Transaction For this User");
        }
        return transactions.stream().map(transactionsMapper::mapUserToTransactionResponse).toList();
    }

    // @Cacheable(value = "transactions", key = "#id")
    public TransactionsDTO getTransactionById(Long id) {
        return transactionsRepository.findById(id)
                .map(transactionsMapper::toTransactionDTO)
                .orElseThrow(() -> new TransactionNotFoundException("Cannot Find Transaction With ID"));
    }

    // @Cacheable(value = "transactions", key = "T(String).format('%d-%s', #userId,
    // #category)")
    public List<TransactionsDTO> getUserTransactionByCategory(Long userId, String category) {
        List<Transactions> transactions = transactionsRepository.findByUserIdAndCategory(userId, category);
        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException("Cannot Find Transaction For Category '" + category + "'");
        }
        return transactions.stream().map(transactionsMapper::toTransactionDTO).toList();
    }

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
                .collect(Collectors.groupingBy(
                        Transactions::getCategory,
                        Collectors.summingDouble(transaction -> transaction.getAmount().doubleValue())));

        return new UserTransactionStatisticsDTO(
                dailySpending,
                dailyCount,
                weeklySpending,
                weeklyCount,
                monthlySpending,
                monthlyCount,
                averageDailySpending,
                averageWeeklySpending,
                averageMonthlySpending,
                categoryBreakdown);

    }

    // TODO: Delete Transaction - Only if Status.FAILED.
    @CacheEvict(value = "transactions", key = "#id")
    public void deleteTransaction(Long id) {

    }
}
