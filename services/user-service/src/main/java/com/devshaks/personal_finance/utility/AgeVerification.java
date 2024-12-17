package com.devshaks.personal_finance.utility;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class AgeVerification {
    public boolean isUserAdult(LocalDate dateOfBirth) {
        LocalDate today = LocalDate.now();
        Period age = Period.between(dateOfBirth, today);
        return age.getYears() >= 18;
    }

}
