package com.devshaks.personal_finance.exceptions;

public class BudgetExceededException extends RuntimeException {
  public BudgetExceededException(String message) {
    super(message != null ? message : "Budget exceeded");
  }
}