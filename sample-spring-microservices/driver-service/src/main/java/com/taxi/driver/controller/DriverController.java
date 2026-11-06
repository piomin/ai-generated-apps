package com.taxi.driver.controller;

import com.taxi.driver.dto.DriverRegistrationRequest;
import com.taxi.driver.dto.DriverResponse;
import com.taxi.driver.dto.LocationUpdateRequest;
import com.taxi.driver.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Validated
public class DriverController {
    private final DriverService driverService;

    @PostMapping("/register")
    public ResponseEntity<DriverResponse> registerDriver(@Valid @RequestBody DriverRegistrationRequest request) {
        DriverResponse response = driverService.registerDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{driverId}/location")
    public ResponseEntity<Void> updateLocation(
            @PathVariable Long driverId,
            @Valid @RequestBody LocationUpdateRequest request) {
        driverService.updateLocation(driverId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{driverId}/active")
    public ResponseEntity<Void> setActiveStatus(
            @PathVariable Long driverId,
            @RequestParam boolean active) {
        driverService.setActiveStatus(driverId, active);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<DriverResponse>> getActiveDrivers() {
        List<DriverResponse> drivers = driverService.getActiveDrivers();
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<DriverResponse>> getNearbyDrivers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5") Double radiusKm,
            @RequestParam(defaultValue = "10") Integer limit) {
        validateNearbyParams(latitude, longitude, radiusKm, limit);
        List<DriverResponse> drivers = driverService.getNearbyDrivers(latitude, longitude, radiusKm, limit);
        return ResponseEntity.ok(drivers);
    }

    private void validateNearbyParams(Double latitude, Double longitude, Double radiusKm, Integer limit) {
        if (latitude == null) {
            throw new IllegalArgumentException("latitude is required");
        }
        if (longitude == null) {
            throw new IllegalArgumentException("longitude is required");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
        if (radiusKm == null || radiusKm <= 0) {
            throw new IllegalArgumentException("radiusKm must be greater than 0");
        }
        if (radiusKm > 50) {
            throw new IllegalArgumentException("radiusKm must not exceed 50");
        }
        if (limit == null || limit < 1 || limit > 50) {
            throw new IllegalArgumentException("limit must be between 1 and 50");
        }
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long driverId) {
        DriverResponse response = driverService.getDriverById(driverId);
        return ResponseEntity.ok(response);
    }
}
