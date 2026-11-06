package com.taxi.trip.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FareCalculationServiceTest {

    @Autowired
    private FareCalculationService service;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldCalculateDistance() {
        Double distance = service.calculateDistance(40.7128, -74.0060, 40.7580, -73.9855);
        assertNotNull(distance);
        assertTrue(distance > 0);
    }

    @Test
    void shouldCalculateFareWithoutTimeBasedMultipliers() {
        LocalDateTime time = LocalDateTime.of(2024, 1, 15, 12, 0);
        BigDecimal fare = service.calculateFare(5.0, time);
        assertNotNull(fare);
        assertTrue(fare.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldCalculateFareWithPeakHourMultiplier() {
        LocalDateTime peakTime = LocalDateTime.of(2024, 1, 15, 8, 0);
        BigDecimal fare = service.calculateFare(5.0, peakTime);
        assertNotNull(fare);
        assertTrue(fare.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldCalculateFareWithNightHourMultiplier() {
        LocalDateTime nightTime = LocalDateTime.of(2024, 1, 15, 23, 0);
        BigDecimal fare = service.calculateFare(5.0, nightTime);
        assertNotNull(fare);
        assertTrue(fare.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldCalculateCancellationFeeWithinGracePeriod() {
        LocalDateTime acceptedAt = LocalDateTime.now().minusMinutes(1);
        BigDecimal fee = service.calculateCancellationFee(acceptedAt, false);
        assertEquals(BigDecimal.ZERO, fee);
    }

    @Test
    void shouldCalculateCancellationFeeOutsideGracePeriod() {
        LocalDateTime acceptedAt = LocalDateTime.now().minusMinutes(3);
        BigDecimal fee = service.calculateCancellationFee(acceptedAt, false);
        assertEquals(new BigDecimal("5.00"), fee);
    }

    @Test
    void shouldNotChargeCancellationFeeForDriverCancellation() {
        LocalDateTime acceptedAt = LocalDateTime.now().minusMinutes(10);
        BigDecimal fee = service.calculateCancellationFee(acceptedAt, true);
        assertEquals(BigDecimal.ZERO, fee);
    }

    @Test
    void shouldCalculateFeeAtExactGracePeriodBoundary() {
        LocalDateTime acceptedAt = LocalDateTime.now().minusMinutes(2);
        BigDecimal fee = service.calculateCancellationFee(acceptedAt, false);
        assertEquals(new BigDecimal("5.00"), fee);
    }
}
