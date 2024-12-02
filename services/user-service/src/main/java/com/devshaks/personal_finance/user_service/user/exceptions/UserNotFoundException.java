package com.devshaks.personal_finance.user_service.user.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserNotFoundException extends RuntimeException {
   private final String exceptionMessage;
}
