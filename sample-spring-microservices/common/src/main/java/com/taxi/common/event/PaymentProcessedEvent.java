package com.taxi.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private Long tripId;
    private Long userId;
    private BigDecimal amount;
    private boolean success;
    private String transactionId;
}
