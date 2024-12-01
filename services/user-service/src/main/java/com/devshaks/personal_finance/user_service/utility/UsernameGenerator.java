package com.devshaks.personal_finance.user_service.utility;

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
}
