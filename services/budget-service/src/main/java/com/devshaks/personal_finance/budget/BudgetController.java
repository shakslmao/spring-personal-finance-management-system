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
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/budgets")
@Tag(name = "Budget Controller", description = "Handles Budget Related Operations")
public class BudgetController {
    private final BudgetService budgetService;

    @PostMapping("/create/{userId}")
    @Operation(summary = "Set a new Budget Limit for a User")
    @ApiResponses(value = { @ApiResponse(responseCode = "201"), @ApiResponse(responseCode = "400" )})
    public ResponseEntity<BudgetResponse> createUserBudget(@PathVariable("userId") Long userId, @RequestBody BudgetRequest budgetRequest) {
        BudgetResponse budgetResponse = budgetService.createUserBudget(userId, budgetRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Budget Details by Budget ID")
    @ApiResponses(value = { @ApiResponse(responseCode = "200"),  @ApiResponse(responseCode = "404" )})
    public ResponseEntity<BudgetResponse> getBudgetById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get a Budget Response From a User ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404" )})
    public ResponseEntity<List<BudgetResponse>> getUserBudgets(@PathVariable("userId") Long userId) {
        List <BudgetResponse> response = budgetService.getUserBudgets(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Delete a Budget
    @DeleteMapping("/user/{userId}/delete/{id}")
    @Operation(summary = "Delete a Budget by Budget ID")
    @ApiResponses(value = { @ApiResponse(responseCode = "200"),  @ApiResponse(responseCode = "404") })
    public ResponseEntity<Void> deleteBudget(@PathVariable("userId") Long userId, @PathVariable("id") Long id) {
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // Update an Existing Budget
    @PutMapping("/update/{userId}/{id}")
    @Operation(summary = "Update Budget Details By Budget ID")
    @ApiResponses(value = { @ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404" )})
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable("userId") Long userId,
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> updates) {
        BudgetResponse updatedBudget = budgetService.updateBudget(userId, id, updates);
        return ResponseEntity.status(HttpStatus.OK).body(updatedBudget);

    }

    // Add a new Category to an Existing Budget

    // Delete a Category from a Budget

    // Check if a Transaction Exceeds a Budget

    // Generate Budget Report.

  }
