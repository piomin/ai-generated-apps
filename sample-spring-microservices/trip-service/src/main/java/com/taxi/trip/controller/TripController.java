package com.taxi.trip.controller;

import com.taxi.trip.dto.TripRequest;
import com.taxi.trip.dto.TripResponse;
import com.taxi.trip.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }

    private String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.hasAuthority("ROLE_DRIVER"))) {
                return "DRIVER";
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.hasAuthority("ROLE_USER"))) {
                return "USER";
            }
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<TripResponse> requestTrip(@Valid @RequestBody TripRequest request) {
        TripResponse response = tripService.requestTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{tripId}/accept")
    public ResponseEntity<TripResponse> acceptTrip(
            @PathVariable Long tripId,
            @RequestParam Long driverId) {
        TripResponse response = tripService.acceptTrip(tripId, driverId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{tripId}/start")
    public ResponseEntity<TripResponse> startTrip(@PathVariable Long tripId) {
        TripResponse response = tripService.startTrip(tripId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{tripId}/complete")
    public ResponseEntity<TripResponse> completeTrip(
            @PathVariable Long tripId,
            @RequestParam String userEmail) {
        TripResponse response = tripService.completeTrip(tripId, userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable Long tripId) {
        TripResponse response = tripService.getTripById(tripId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TripResponse>> getUserTripHistory(@PathVariable Long userId) {
        List<TripResponse> trips = tripService.getUserTripHistory(userId);
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<TripResponse>> getDriverTripHistory(@PathVariable Long driverId) {
        List<TripResponse> trips = tripService.getDriverTripHistory(driverId);
        return ResponseEntity.ok(trips);
    }

    @PutMapping("/{tripId}/cancel")
    public ResponseEntity<TripResponse> cancelTrip(
            @PathVariable Long tripId,
            @RequestParam(required = false) String cancellationReason) {
        String userId = getCurrentUserId();
        String role = getCurrentRole();
        TripResponse response = tripService.cancelTrip(tripId, userId, role, cancellationReason);
        return ResponseEntity.ok(response);
    }
}
