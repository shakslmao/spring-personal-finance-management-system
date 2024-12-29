package com.devshaks.personal_finance.transactions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction Controller", description = "Handles Transaction Related Operations")
public class TransactionsController {
    private final TransactionsService transactionService;

    @PostMapping("/new/{userId}")
    @Operation(summary = "Create a New Transaction For a User")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Transaction Created Successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data") })
    public ResponseEntity<TransactionsDTO> newTransaction(@PathVariable("userId") Long userId,
            @RequestBody @Valid TransactionsRequest transactionRequest) {
        TransactionsDTO transaction = transactionService.newTransaction(transactionRequest, userId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(transaction.id())
                .toUri();
        return ResponseEntity.created(location).body(transaction);
    }

    // Get Transaction By ID.
    @GetMapping("/{id}")
    @Operation(summary = "Get Transaction by ID.")
    public ResponseEntity<TransactionsDTO> getTransactionById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    // Get Users Transactions By Their ID.
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get Users Recent Transactions")
    public ResponseEntity<List<TransactionsResponse>> getUsersTransactions(@PathVariable("userId") Long userId) {
        List<TransactionsResponse> transactionResponse = transactionService.getUsersTransactions(userId);
        return ResponseEntity.ok(transactionResponse);
    }

    // Get Transactions By User ID & Category.
    @GetMapping("/user/{userId}/category/{category}")
    @Operation(summary = "Get Users Transactions By Their ID & Categories")
    public ResponseEntity<List<TransactionsDTO>> getUserTransactionByCategory(@PathVariable("userId") Long userId,
            @PathVariable("category") String category) {
        return ResponseEntity.ok(transactionService.getUserTransactionByCategory(userId, category));
    }

    // Get Transaction With Filtering and Pagination.
    @GetMapping("/filter")
    @Operation(summary = "Filter Transactions Through Various Parameters")
    public ResponseEntity<PaginatedTransactionDTO> getTransactionFilter(@RequestParam(required = false) Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDateTime transactionDate,
            @RequestParam(required = false) TransactionsType transactionsType,
            @RequestParam(required = false) TransactionsStatus transactionsStatus,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PaginatedTransactionDTO transactionsFilter = transactionService.getTransactionFilter(userId, category,
                transactionDate, transactionsType, transactionsStatus, pageable);
        return ResponseEntity.ok(transactionsFilter);
    }

    // Get Transactions By Statistics For User.
    @GetMapping("/user/{userId}/statistics")
    @Operation(summary = "Return User Transaction Stats (e.g., Weekly Spend, Monthly Spend, Predicted Spending")
    public ResponseEntity<UserTransactionStatisticsDTO> getUserTransactionStats(@PathVariable("userId") Long userId) {
        UserTransactionStatisticsDTO statisticsDTO = transactionService.getUserTransactionStats(userId);
        return ResponseEntity.ok(statisticsDTO);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable("id") Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

}
