package com.devshaks.personal_finance.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TwilioSMSException extends RuntimeException {
    private final String exceptionMessage;

}
