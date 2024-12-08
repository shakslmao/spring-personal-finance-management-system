package com.devshaks.personal_finance.utility;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class UsernameGenerator {
    public String generateUsername(int yearOfBirth) {
        Random random = new Random();
        int randomFourDigits = 1000 + random.nextInt(9000);
        int lastTwoDigitsOfYear = yearOfBirth % 100;
        return randomFourDigits + String.format("%02d", lastTwoDigitsOfYear);
    }

    public String generateAdminUsername(int yearOfBirth) {
        Random random = new Random();
        int randomFiveDigits = 10000 + random.nextInt(90000);
        int lastTwoDigitsOfYear = yearOfBirth % 100;
        return "ADM-" + randomFiveDigits + "-" + lastTwoDigitsOfYear;
    }
}
