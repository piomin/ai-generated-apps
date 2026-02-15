package com.taxi.trip.controller;

import com.taxi.trip.dto.TripRequest;
import com.taxi.trip.dto.TripResponse;
import com.taxi.trip.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;

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
}
