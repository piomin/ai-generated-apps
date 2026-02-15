package com.taxi.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationUpdateEvent {
    private Long driverId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
}
