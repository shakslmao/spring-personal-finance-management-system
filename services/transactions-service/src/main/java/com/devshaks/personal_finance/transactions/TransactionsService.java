package com.devshaks.personal_finance.transactions;

import com.devshaks.personal_finance.exceptions.TransactionNotFoundException;
import com.devshaks.personal_finance.exceptions.UserNotFoundException;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.kafka.user.UserEventSender;
import com.devshaks.personal_finance.users.UserDetailsResponse;
import com.devshaks.personal_finance.users.UserFeignClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.devshaks.personal_finance.kafka.events.TransactionEvents.TRANSACTION_CREATED;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionsService {
    private final TransactionsRepository transactionsRepository;
    private final TransactionsMapper transactionsMapper;
    private final UserFeignClient userFeignClient;
    private final AuditEventSender auditEventSender;
    private final UserEventSender userEventSender;


    public TransactionsDTO newTransaction(@Valid TransactionsRequest transactionRequest, Long userId) {
        UserDetailsResponse user = userFeignClient.getUserProfileDetails(userId);
        if (user == null || user.userId() == null) { throw new UserNotFoundException("User not found"); }
        Transactions transactions = transactionsMapper.toNewTransaction(transactionRequest);
        transactions.setUserId(userId);
        Transactions savedTransaction = transactionsRepository.save(transactions);
        auditEventSender.sendEventToAudit(TRANSACTION_CREATED, userId, "New Transaction Created");
        userEventSender.sendEventToUser(userId, transactions.getId(), "New Transaction Recorded", transactions.getAmount());
        return transactionsMapper.toTransactionDTO(savedTransaction);
    }


    // @Cacheable(value = "transactions", key = "#userId")
    public List<TransactionsResponse> getUsersTransactions(Long userId) {
        List<Transactions> transactions = transactionsRepository.findByUserId(userId);
        if (transactions.isEmpty()) { throw new TransactionNotFoundException("Cannot Find Transaction For this User"); }
        return transactions.stream().map(transactionsMapper::mapUserToTransactionResponse).toList();
    }

    // @Cacheable(value = "transactions", key = "#id")
    public TransactionsDTO getTransactionById(Long id) {
        return transactionsRepository.findById(id)
                .map(transactionsMapper::toTransactionDTO)
                .orElseThrow(() -> new TransactionNotFoundException("Cannot Find Transaction With ID"));
    }


    // @Cacheable(value = "transactions", key = "T(String).format('%d-%s', #userId, #category)")
    public List<TransactionsDTO> getUserTransactionByCategory(Long userId, String category) {
        List<Transactions> transactions = transactionsRepository.findByUserIdAndCategory(userId, category);
        if (transactions.isEmpty()) { throw new TransactionNotFoundException("Cannot Find Transaction For Category '" + category + "'"); }
        return transactions.stream().map(transactionsMapper::toTransactionDTO).toList();
    }

    // @Cacheable(value = "transaction-filters", key = "#userId + '-' + #category + '-' + #pageable.pageNumber")
    public Page<TransactionsDTO> getTransactionFilter(Long userId, String category, LocalDateTime transactionDate, TransactionsType transactionsType, TransactionsStatus transactionsStatus, Pageable pageable) {
        Specification<Transactions> specification = Specification.where(null);
        if (userId != null) { specification.and((root, query, cb) -> cb.equal(root.get("userId"), userId)); }
        if (category != null) { specification.and((root, query, cb) -> cb.equal(root.get("category"), category)); }
        if (transactionDate != null) { specification.and((root, query, cb) -> cb.equal(root.get("transactionDate"), transactionDate)); }
        if (transactionsType != null) { specification.and((root, query, cb) -> cb.equal(root.get("transactionType"), transactionsType)); }
        if (transactionsStatus != null) { specification = specification.and((root, query, cb) -> cb.equal(root.get("transactionStatus"), transactionsStatus)); }

        Page<Transactions> transactionsPage = transactionsRepository.findAll(specification, pageable);
        return transactionsPage.map(transactionsMapper::toTransactionDTO);
    }

    public UserTransactionStatisticsDTO getUserTransactionStats(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        List<Transactions> dailyTransactions = transactionsRepository.findByUserIdAndTransactionDateBetween(userId, startOfDay, now);
        List<Transactions> weeklyTransactions = transactionsRepository.findByUserIdAndTransactionDateBetween(userId, startOfWeek, now);
        List<Transactions> monthlyTransactions = transactionsRepository.findByUserIdAndTransactionDateBetween(userId, startOfMonth, now);

        double dailySpending = dailyTransactions.stream().mapToDouble(transaction -> transaction.getAmount().doubleValue()).sum();
        int dailyCount = dailyTransactions.size();

        double weeklySpending = weeklyTransactions.stream().mapToDouble(transaction -> transaction.getAmount().doubleValue()).sum();
        int weeklyCount = weeklyTransactions.size();

        double monthlySpending = monthlyTransactions.stream().mapToDouble(transaction -> transaction.getAmount().doubleValue()).sum();
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
