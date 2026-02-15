package com.taxi.driver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverRegistrationRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotNull(message = "Car details are required")
    private CarDetails carDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarDetails {
        @NotBlank(message = "Car make is required")
        private String make;

        @NotBlank(message = "Car model is required")
        private String model;

        @NotNull(message = "Car year is required")
        private Integer year;

        @NotBlank(message = "License plate is required")
        private String licensePlate;

        @NotBlank(message = "Car color is required")
        private String color;
    }
}
