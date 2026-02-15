package com.taxi.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String licenseNumber;
    private boolean active;
    private CarInfo car;
    private LocationInfo currentLocation;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarInfo {
        private String make;
        private String model;
        private Integer year;
        private String licensePlate;
        private String color;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
        private Double latitude;
        private Double longitude;
    }
}
