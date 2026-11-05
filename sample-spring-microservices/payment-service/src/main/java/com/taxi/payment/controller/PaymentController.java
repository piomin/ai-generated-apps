package com.taxi.payment.controller;

import com.taxi.payment.dto.PaymentResponse;
import com.taxi.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{tripId}")
    public ResponseEntity<PaymentResponse> getPaymentByTripId(@PathVariable Long tripId) {
        PaymentResponse payment;
        try {
            payment = paymentService.getPaymentByTripId(tripId);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Payment not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.attributes['sub']")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Long userId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }
}
