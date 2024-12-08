package com.devshaks.personal_finance.utility;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AdminCodeGenerator {
    public String generateAdminCode(int yearOfBirth) {
        Random random = new Random();
        int randomFiveDigits = 10000 + random.nextInt(90000);
        int lastTwoDigitsOfYear = yearOfBirth % 100;
        return "ADM-" + randomFiveDigits + "-" + lastTwoDigitsOfYear;
    }
}
