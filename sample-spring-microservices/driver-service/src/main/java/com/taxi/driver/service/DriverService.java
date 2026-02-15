package com.taxi.driver.service;

import com.taxi.common.config.KafkaTopics;
import com.taxi.common.event.DriverLocationUpdateEvent;
import com.taxi.driver.dto.DriverRegistrationRequest;
import com.taxi.driver.dto.DriverResponse;
import com.taxi.driver.dto.LocationUpdateRequest;
import com.taxi.driver.model.Car;
import com.taxi.driver.model.Driver;
import com.taxi.driver.model.DriverLocation;
import com.taxi.driver.repository.DriverLocationRepository;
import com.taxi.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriverRepository driverRepository;
    private final DriverLocationRepository driverLocationRepository;
    private final KafkaTemplate<String, DriverLocationUpdateEvent> kafkaTemplate;

    @Transactional
    public DriverResponse registerDriver(DriverRegistrationRequest request) {
        Driver driver = new Driver();
        driver.setFirstName(request.getFirstName());
        driver.setLastName(request.getLastName());
        driver.setEmail(request.getEmail());
        driver.setPhoneNumber(request.getPhoneNumber());
        driver.setLicenseNumber(request.getLicenseNumber());

        Car car = new Car();
        car.setDriver(driver);
        car.setMake(request.getCarDetails().getMake());
        car.setModel(request.getCarDetails().getModel());
        car.setYear(request.getCarDetails().getYear());
        car.setLicensePlate(request.getCarDetails().getLicensePlate());
        car.setColor(request.getCarDetails().getColor());

        driver.setCar(car);
        driver = driverRepository.save(driver);

        return mapToResponse(driver);
    }

    @Transactional
    public void updateLocation(Long driverId, LocationUpdateRequest request) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        DriverLocation location = driverLocationRepository.findByDriverId(driverId)
                .orElse(new DriverLocation());

        location.setDriver(driver);
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setTimestamp(LocalDateTime.now());

        driverLocationRepository.save(location);

        // Publish location update event to Kafka
        DriverLocationUpdateEvent event = new DriverLocationUpdateEvent(
                driverId,
                request.getLatitude(),
                request.getLongitude(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(KafkaTopics.DRIVER_LOCATION_UPDATE, event);
    }

    @Transactional
    public void setActiveStatus(Long driverId, boolean active) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setActive(active);
        driverRepository.save(driver);
    }

    public List<DriverResponse> getActiveDrivers() {
        return driverRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DriverResponse getDriverById(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        return mapToResponse(driver);
    }

    private DriverResponse mapToResponse(Driver driver) {
        DriverResponse response = new DriverResponse();
        response.setId(driver.getId());
        response.setFirstName(driver.getFirstName());
        response.setLastName(driver.getLastName());
        response.setEmail(driver.getEmail());
        response.setPhoneNumber(driver.getPhoneNumber());
        response.setLicenseNumber(driver.getLicenseNumber());
        response.setActive(driver.isActive());

        if (driver.getCar() != null) {
            DriverResponse.CarInfo carInfo = new DriverResponse.CarInfo();
            carInfo.setMake(driver.getCar().getMake());
            carInfo.setModel(driver.getCar().getModel());
            carInfo.setYear(driver.getCar().getYear());
            carInfo.setLicensePlate(driver.getCar().getLicensePlate());
            carInfo.setColor(driver.getCar().getColor());
            response.setCar(carInfo);
        }

        if (driver.getCurrentLocation() != null) {
            DriverResponse.LocationInfo locationInfo = new DriverResponse.LocationInfo();
            locationInfo.setLatitude(driver.getCurrentLocation().getLatitude());
            locationInfo.setLongitude(driver.getCurrentLocation().getLongitude());
            response.setCurrentLocation(locationInfo);
        }

        return response;
    }
}
