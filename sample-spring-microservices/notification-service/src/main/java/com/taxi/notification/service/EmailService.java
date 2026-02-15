package com.taxi.notification.service;

import com.taxi.common.config.KafkaTopics;
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

    private void sendTripSummaryEmail(TripCompletedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.getUserEmail());
        message.setSubject("Trip Summary - Trip #" + event.getTripId());
        message.setText(buildEmailContent(event));

        // In production with real SMTP configuration, this would send the email
        // For demo, we just log it
        log.info("Email content:\n{}", message.getText());

        // Uncomment to actually send email when SMTP is configured
        // mailSender.send(message);
    }

    private String buildEmailContent(TripCompletedEvent event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return String.format("""
                Dear Customer,

                Thank you for using our taxi service! Here is your trip summary:

                Trip ID: %d
                Pickup Location: %s
                Dropoff Location: %s
                Distance: %.2f km
                Cost: $%.2f

                Trip Start: %s
                Trip End: %s

                We hope you enjoyed your ride!

                Best regards,
                Taxi Reservation System
                """,
                event.getTripId(),
                event.getPickupLocation(),
                event.getDropoffLocation(),
                event.getDistance(),
                event.getCost(),
                event.getStartTime().format(formatter),
                event.getEndTime().format(formatter)
        );
    }
}
