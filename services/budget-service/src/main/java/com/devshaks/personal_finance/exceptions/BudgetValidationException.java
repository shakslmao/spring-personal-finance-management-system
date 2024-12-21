package com.devshaks.personal_finance.exceptions;

public class BudgetValidationException extends RuntimeException {
  public BudgetValidationException(String message) {
    super(message);
  }
}
