package com.devshaks.personal_finance.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BudgetUpdateException extends RuntimeException {
    public final String exceptionMessage;
}
