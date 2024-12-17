package com.devshaks.personal_finance.transactions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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
    public ResponseEntity<TransactionsDTO> newTransaction(@PathVariable("userId") Long userId, @RequestBody @Valid TransactionsRequest transactionRequest) {
        log.info("Creating new transaction for userId: {} with request: {}", userId, transactionRequest);
        TransactionsDTO transaction = transactionService.newTransaction(transactionRequest, userId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(transaction.id())
                .toUri();
        return ResponseEntity.created(location).body(transaction);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get Users Recent Transactions")
    public ResponseEntity<List<TransactionsResponse>> getUsersTransactions(@PathVariable("userId") Long userId) {
        List<TransactionsResponse> transactionResponse = transactionService.getUsersTransactions(userId);
        return ResponseEntity.ok(transactionResponse);
    }

}
