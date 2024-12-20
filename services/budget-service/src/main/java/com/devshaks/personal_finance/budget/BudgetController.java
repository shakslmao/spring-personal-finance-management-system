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

import java.util.List;

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

    @GetMapping("/{id}")
    @Operation(summary = "Get Budget Details by Budget ID")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Budget Found"),  @ApiResponse(responseCode = "404", description = "Budget Not Found")})
    public ResponseEntity<BudgetResponse> getBudgetById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get a Budget Response From a User ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Budgets Found"), @ApiResponse(responseCode = "404", description = "No Budgets Found for User")})
    public ResponseEntity<List<BudgetResponse>> getUserBudgets(@PathVariable("userId") Long userId) {
        List <BudgetResponse> response = budgetService.getUserBudgets(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Delete a Budget
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Budget by Budget ID")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Budget Deleted Successfully"),  @ApiResponse(responseCode = "404", description = "Budget Not Found")})
    public ResponseEntity<String> deleteBudget(@PathVariable("id") Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok("Budget Deleted Successfully");
    }


    // Update an Existing Budget

    // Add a new Category to an Existing Budget

    // Update a Category from a Budget

    // Delete a Category from a Budget

    // Check if a Transaction Exceeds a Budget

    // Generate Budget Report.

  }
