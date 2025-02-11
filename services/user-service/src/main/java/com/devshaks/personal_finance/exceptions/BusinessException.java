package com.devshaks.personal_finance.exceptions;

import com.devshaks.personal_finance.handlers.BusinessErrorCodes;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final BusinessErrorCodes businessErrorCode;

    public BusinessException(BusinessErrorCodes businessErrorCode) {
        super(businessErrorCode.getDescription());
        this.businessErrorCode = businessErrorCode;
    }

    public BusinessErrorCodes getBusinessErrorCode() {
        return businessErrorCode;
    }
}
