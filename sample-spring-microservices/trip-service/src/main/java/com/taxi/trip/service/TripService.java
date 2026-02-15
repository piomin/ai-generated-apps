package com.taxi.trip.service;

import com.taxi.common.config.KafkaTopics;
import com.taxi.common.event.TripCompletedEvent;
import com.taxi.trip.dto.TripRequest;
import com.taxi.trip.dto.TripResponse;
import com.taxi.trip.model.Trip;
import com.taxi.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final FareCalculationService fareCalculationService;
    private final KafkaTemplate<String, TripCompletedEvent> kafkaTemplate;

    @Transactional
    public TripResponse requestTrip(TripRequest request) {
        // Calculate distance
        Double distance = fareCalculationService.calculateDistance(
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDropoffLatitude(),
                request.getDropoffLongitude()
        );

        // Calculate fare
        LocalDateTime requestTime = LocalDateTime.now();
        BigDecimal estimatedCost = fareCalculationService.calculateFare(distance, requestTime);

        Trip trip = new Trip();
        trip.setUserId(request.getUserId());
        trip.setPickupLocation(request.getPickupLocation());
        trip.setPickupLatitude(request.getPickupLatitude());
        trip.setPickupLongitude(request.getPickupLongitude());
        trip.setDropoffLocation(request.getDropoffLocation());
        trip.setDropoffLatitude(request.getDropoffLatitude());
        trip.setDropoffLongitude(request.getDropoffLongitude());
        trip.setDistance(distance);
        trip.setEstimatedCost(estimatedCost);
        trip.setStatus(Trip.TripStatus.REQUESTED);

        trip = tripRepository.save(trip);

        return mapToResponse(trip);
    }

    @Transactional
    public TripResponse acceptTrip(Long tripId, Long driverId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getStatus() != Trip.TripStatus.REQUESTED) {
            throw new RuntimeException("Trip cannot be accepted in current status");
        }

        trip.setDriverId(driverId);
        trip.setStatus(Trip.TripStatus.ACCEPTED);
        trip = tripRepository.save(trip);

        return mapToResponse(trip);
    }

    @Transactional
    public TripResponse startTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getStatus() != Trip.TripStatus.ACCEPTED) {
            throw new RuntimeException("Trip cannot be started in current status");
        }

        trip.setStatus(Trip.TripStatus.IN_PROGRESS);
        trip.setStartedAt(LocalDateTime.now());
        trip = tripRepository.save(trip);

        return mapToResponse(trip);
    }

    @Transactional
    public TripResponse completeTrip(Long tripId, String userEmail) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getStatus() != Trip.TripStatus.IN_PROGRESS) {
            throw new RuntimeException("Trip cannot be completed in current status");
        }

        trip.setStatus(Trip.TripStatus.COMPLETED);
        trip.setCompletedAt(LocalDateTime.now());

        // Recalculate actual cost based on completion time
        BigDecimal actualCost = fareCalculationService.calculateFare(trip.getDistance(), trip.getCompletedAt());
        trip.setActualCost(actualCost);

        trip = tripRepository.save(trip);

        // Publish trip completed event to Kafka
        TripCompletedEvent event = new TripCompletedEvent(
                trip.getId(),
                trip.getUserId(),
                trip.getDriverId(),
                userEmail,
                trip.getPickupLocation(),
                trip.getDropoffLocation(),
                trip.getActualCost(),
                trip.getDistance(),
                trip.getStartedAt(),
                trip.getCompletedAt()
        );
        kafkaTemplate.send(KafkaTopics.TRIP_COMPLETED, event);

        return mapToResponse(trip);
    }

    public TripResponse getTripById(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        return mapToResponse(trip);
    }

    public List<TripResponse> getUserTripHistory(Long userId) {
        return tripRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TripResponse> getDriverTripHistory(Long driverId) {
        return tripRepository.findByDriverIdOrderByCreatedAtDesc(driverId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TripResponse mapToResponse(Trip trip) {
        TripResponse response = new TripResponse();
        response.setId(trip.getId());
        response.setUserId(trip.getUserId());
        response.setDriverId(trip.getDriverId());
        response.setPickupLocation(trip.getPickupLocation());
        response.setDropoffLocation(trip.getDropoffLocation());
        response.setEstimatedCost(trip.getEstimatedCost());
        response.setActualCost(trip.getActualCost());
        response.setDistance(trip.getDistance());
        response.setStatus(trip.getStatus());
        response.setRequestedAt(trip.getRequestedAt());
        response.setStartedAt(trip.getStartedAt());
        response.setCompletedAt(trip.getCompletedAt());
        return response;
    }
}
