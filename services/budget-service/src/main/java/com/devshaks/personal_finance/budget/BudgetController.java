package com.devshaks.personal_finance.budget;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/budgets")
@Tag(name = "Budget Controller", description = "Handles Budget Related Operations")
public class BudgetController {
    private final BudgetService budgetService;

    @PostMapping("/create/{userId}")
    @Operation(summary = "Set a new Budget Limit for a User")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Budget Set Successfully"), @ApiResponse(responseCode = "400", description = "Failed to Set Budget") })
    public ResponseEntity<BudgetResponse> createUserBudget(@PathVariable("userId") Long userId, @RequestBody BudgetRequest budgetRequest) {
        BudgetResponse budgetResponse = budgetService.createUserBudget(userId, budgetRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetResponse);
    }
}
