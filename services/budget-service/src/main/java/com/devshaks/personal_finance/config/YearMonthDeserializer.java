package com.devshaks.personal_finance.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.YearMonth;

public class YearMonthDeserializer extends JsonDeserializer<YearMonth> {

    @Override
    public YearMonth deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return YearMonth.parse(p.getText()); // Parse "yyyy-MM" format
    }
}
