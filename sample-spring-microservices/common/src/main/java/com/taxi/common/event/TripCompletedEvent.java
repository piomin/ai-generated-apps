package com.taxi.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripCompletedEvent {
    private Long tripId;
    private Long userId;
    private Long driverId;
    private String userEmail;
    private String pickupLocation;
    private String dropoffLocation;
    private BigDecimal cost;
    private Double distance;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
