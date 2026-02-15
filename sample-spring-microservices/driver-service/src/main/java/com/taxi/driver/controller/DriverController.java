package com.taxi.driver.controller;

import com.taxi.driver.dto.DriverRegistrationRequest;
import com.taxi.driver.dto.DriverResponse;
import com.taxi.driver.dto.LocationUpdateRequest;
import com.taxi.driver.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
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

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long driverId) {
        DriverResponse response = driverService.getDriverById(driverId);
        return ResponseEntity.ok(response);
    }
}
