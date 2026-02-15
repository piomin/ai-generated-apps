package com.taxi.trip.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class FareCalculationService {
    // Sample pricing configuration
    private static final BigDecimal BASE_FARE = new BigDecimal("5.00");
    private static final BigDecimal COST_PER_KM = new BigDecimal("2.50");
    private static final BigDecimal PEAK_HOUR_MULTIPLIER = new BigDecimal("1.5");
    private static final BigDecimal NIGHT_HOUR_MULTIPLIER = new BigDecimal("1.3");

    public BigDecimal calculateFare(Double distance, LocalDateTime requestTime) {
        BigDecimal fare = BASE_FARE;

        // Add distance cost
        BigDecimal distanceCost = COST_PER_KM.multiply(BigDecimal.valueOf(distance));
        fare = fare.add(distanceCost);

        // Apply time-based multipliers
        int hour = requestTime.getHour();

        // Peak hours: 7-9 AM and 5-7 PM
        if ((hour >= 7 && hour < 9) || (hour >= 17 && hour < 19)) {
            fare = fare.multiply(PEAK_HOUR_MULTIPLIER);
        }
        // Night hours: 10 PM to 6 AM
        else if (hour >= 22 || hour < 6) {
            fare = fare.multiply(NIGHT_HOUR_MULTIPLIER);
        }

        return fare.setScale(2, RoundingMode.HALF_UP);
    }

    public Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        // Haversine formula to calculate distance in kilometers
        final int EARTH_RADIUS = 6371; // Radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
