package com.taxi.trip.dto;

import com.taxi.trip.model.Trip;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {
    private Long id;
    private Long userId;
    private Long driverId;
    private String pickupLocation;
    private String dropoffLocation;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private Double distance;
    private Trip.TripStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
