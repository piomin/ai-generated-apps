package com.taxi.user.dto;

import com.taxi.user.validation.ValidCardNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCardRequest {
    @NotBlank(message = "Card number is required")
    @ValidCardNumber(message = "Card number must be 13-19 digits and pass Luhn checksum validation")
    private String cardNumber;

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    @NotBlank(message = "Expiry month is required")
    @Pattern(regexp = "(0[1-9]|1[0-2])", message = "Expiry month must be between 01 and 12")
    private String expiryMonth;

    @NotBlank(message = "Expiry year is required")
    @Pattern(regexp = "\\d{4}", message = "Expiry year must be 4 digits")
    private String expiryYear;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 digits")
    private String cvv;

    @Override
    public String toString() {
        String maskedCardNumber = cardNumber != null && cardNumber.length() >= 4
                ? "************" + cardNumber.substring(cardNumber.length() - 4)
                : "****";
        return "PaymentCardRequest{" +
                "cardNumber='" + maskedCardNumber + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", expiryMonth='" + expiryMonth + '\'' +
                ", expiryYear='" + expiryYear + '\'' +
                ", cvv='***'" +
                '}';
    }
}
