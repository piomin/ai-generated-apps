package com.taxi.payment.dto;

import com.taxi.payment.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long tripId;
    private Long userId;
    private BigDecimal amount;
    private Payment.PaymentStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
}
