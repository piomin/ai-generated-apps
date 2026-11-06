package com.taxi.notification.service;

import com.taxi.common.config.KafkaTopics;
import com.taxi.common.event.TripCancelledEvent;
import com.taxi.common.event.TripCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @KafkaListener(topics = KafkaTopics.TRIP_COMPLETED, groupId = "notification-service")
    public void handleTripCompletedEvent(TripCompletedEvent event) {
        log.info("Received trip completed event for trip: {}", event.getTripId());

        try {
            sendTripSummaryEmail(event);
            log.info("Trip summary email sent successfully to: {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to send email to: {}", event.getUserEmail(), e);
        }
    }

    @KafkaListener(topics = KafkaTopics.TRIP_CANCELLED, groupId = "notification-service")
    public void handleTripCancelledEvent(TripCancelledEvent event) {
        log.info("Received trip cancelled event for trip: {}", event.getTripId());

        try {
            sendCancellationNotification(event);
            log.info("Cancellation notification sent to user: {}", event.getUserEmail());
            if (event.getDriverId() != null) {
                log.info("Cancellation notification sent to driver: {}", event.getDriverId());
            }
        } catch (Exception e) {
            log.error("Failed to send cancellation notification", e);
        }
    }

    private void sendTripSummaryEmail(TripCompletedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.getUserEmail());
        message.setSubject("Trip Summary - Trip #" + event.getTripId());
        message.setText(buildEmailContent(event));

        log.info("Email content:\n{}", message.getText());
    }

    private void sendCancellationNotification(TripCancelledEvent event) {
        SimpleMailMessage userMessage = new SimpleMailMessage();
        userMessage.setTo(event.getUserEmail());
        userMessage.setSubject("Trip Cancelled - Trip #" + event.getTripId());
        userMessage.setText(buildCancellationEmail(event, "user"));
        log.info("Cancellation email to user:\n{}", userMessage.getText());

        if (event.getDriverId() != null) {
            SimpleMailMessage driverMessage = new SimpleMailMessage();
            driverMessage.setTo("driver" + event.getDriverId() + "@example.com");
            driverMessage.setSubject("Trip Cancelled - Trip #" + event.getTripId());
            driverMessage.setText(buildCancellationEmail(event, "driver"));
            log.info("Cancellation email to driver:\n{}", driverMessage.getText());
        }
    }

    private String buildCancellationEmail(TripCancelledEvent event, String recipientType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if ("user".equals(recipientType)) {
            StringBuilder content = new StringBuilder();
            content.append("Dear Customer,\n\n");
            content.append("Your trip has been cancelled.\n\n");
            content.append("Trip ID: ").append(event.getTripId()).append("\n");
            content.append("Pickup Location: ").append(event.getPickupLocation()).append("\n");
            content.append("Dropoff Location: ").append(event.getDropoffLocation()).append("\n");
            content.append("Cancelled At: ").append(event.getCancelledAt().format(formatter)).append("\n");
            content.append("Cancelled By: ").append(event.getCancelledBy()).append("\n");

            if (event.getCancellationReason() != null && !event.getCancellationReason().isEmpty()) {
                content.append("Reason: ").append(event.getCancellationReason()).append("\n");
            }

            if (event.getCancellationFee() != null && event.getCancellationFee().compareTo(java.math.BigDecimal.ZERO) > 0) {
                content.append("Cancellation Fee: $").append(event.getCancellationFee()).append("\n");
            } else {
                content.append("No cancellation fee applied.\n");
            }

            content.append("\nIf you have any questions, please contact our support team.\n\n");
            content.append("Best regards,\nTaxi Reservation System");

            return content.toString();
        } else {
            StringBuilder content = new StringBuilder();
            content.append("Dear Driver,\n\n");
            content.append("A trip assigned to you has been cancelled.\n\n");
            content.append("Trip ID: ").append(event.getTripId()).append("\n");
            content.append("Pickup Location: ").append(event.getPickupLocation()).append("\n");
            content.append("Dropoff Location: ").append(event.getDropoffLocation()).append("\n");
            content.append("Cancelled At: ").append(event.getCancelledAt().format(formatter)).append("\n");
            content.append("Cancelled By: ").append(event.getCancelledBy()).append("\n");

            if (event.getCancellationReason() != null && !event.getCancellationReason().isEmpty()) {
                content.append("Reason: ").append(event.getCancellationReason()).append("\n");
            }

            content.append("\nYou will not be charged any fees for this cancellation.\n\n");
            content.append("Best regards,\nTaxi Reservation System");

            return content.toString();
        }
    }
}
