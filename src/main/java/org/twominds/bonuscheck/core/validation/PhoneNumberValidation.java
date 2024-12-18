package org.twominds.bonuscheck.core.validation;


import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PhoneNumberValidation {
    public boolean isPhoneNumberValid(String input) {
        String phoneRegex = "^\\+?\\d{10,15}$"; // Допускаем от 10 до 15 цифр, опционально с +
        return Pattern.matches(phoneRegex, input);
    }
}
