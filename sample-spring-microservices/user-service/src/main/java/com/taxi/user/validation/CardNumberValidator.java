package com.taxi.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CardNumberValidator implements ConstraintValidator<ValidCardNumber, String> {

    @Override
    public boolean isValid(String cardNumber, ConstraintValidatorContext context) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }

        // Remove any spaces or dashes
        String cleanCardNumber = cardNumber.replaceAll("[\\s-]", "");

        // Check length (13-19 digits for valid PANs)
        if (!cleanCardNumber.matches("\\d{13,19}")) {
            return false;
        }

        // Perform Luhn check
        return passesLuhnCheck(cleanCardNumber);
    }

    private boolean passesLuhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        // Process digits from right to left
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }
}
