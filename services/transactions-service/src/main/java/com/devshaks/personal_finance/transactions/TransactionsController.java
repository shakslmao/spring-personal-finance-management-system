package com.devshaks.personal_finance.transactions;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction Controller", description = "Handles Transaction Related Operations")
public class TransactionsController {

    private final TransactionsService transactionService;

    @PostMapping("/new")
    public ResponseEntity<TransactionsDTO> newTransaction(@RequestBody @Valid TransactionsRequest transactionRequest) {
        TransactionsDTO transaction = transactionService.newTransaction(transactionRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(transaction.id())
                .toUri();
        return ResponseEntity.created(location).body(transaction);
    }
}
