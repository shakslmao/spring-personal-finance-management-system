package com.devshaks.personal_finance.user_service.user.handlers;

import java.util.Map;

public record ErrorResponse(
        Map<String, String> errors
) {
}
