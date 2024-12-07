package com.devshaks.personal_finance.handlers;

import java.util.Map;

public record ErrorResponse(
        Map<String, String> errors
) {
}
