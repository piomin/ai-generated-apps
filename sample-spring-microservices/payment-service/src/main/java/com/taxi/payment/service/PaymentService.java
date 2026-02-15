package com.taxi.payment.service;

import com.taxi.common.config.KafkaTopics;
import com.taxi.common.event.PaymentProcessedEvent;
import com.taxi.common.event.TripCompletedEvent;
import com.taxi.payment.model.Payment;
import com.taxi.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

    @KafkaListener(topics = KafkaTopics.TRIP_COMPLETED, groupId = "payment-service")
    @Transactional
    public void processTripCompletedEvent(TripCompletedEvent event) {
        log.info("Processing payment for trip: {}", event.getTripId());

        // Idempotency check: skip if payment already exists for this trip
        if (paymentRepository.existsByTripId(event.getTripId())) {
            log.info("Payment already exists for trip: {}, skipping duplicate processing", event.getTripId());
            return;
        }

        Payment payment = new Payment();
        payment.setTripId(event.getTripId());
        payment.setUserId(event.getUserId());
        payment.setAmount(event.getCost());
        payment.setStatus(Payment.PaymentStatus.PENDING);

        // Simulate payment processing
        boolean paymentSuccess = processPayment(payment);

        if (paymentSuccess) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setTransactionId(UUID.randomUUID().toString());
            log.info("Payment completed for trip: {}", event.getTripId());
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            log.error("Payment failed for trip: {}", event.getTripId());
        }

        try {
            paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            // Handle unique constraint violation as idempotency (concurrent delivery)
            log.info("Payment already processed for trip: {} (concurrent delivery)", event.getTripId());
            return;
        }

        // Publish payment processed event
        PaymentProcessedEvent paymentEvent = new PaymentProcessedEvent(
                event.getTripId(),
                event.getUserId(),
                payment.getAmount(),
                paymentSuccess,
                payment.getTransactionId()
        );
        kafkaTemplate.send(KafkaTopics.PAYMENT_PROCESSED, paymentEvent);
    }

    private boolean processPayment(Payment payment) {
        // Simulate payment processing logic
        // In production, this would integrate with a payment gateway
        try {
            Thread.sleep(1000); // Simulate processing time
            return true; // Always successful for demo purposes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
