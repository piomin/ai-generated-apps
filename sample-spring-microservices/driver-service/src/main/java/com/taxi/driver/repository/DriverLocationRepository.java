package com.taxi.driver.repository;

import com.taxi.driver.model.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {
    Optional<DriverLocation> findByDriverId(Long driverId);

    @Query("SELECT dl FROM DriverLocation dl JOIN dl.driver d WHERE d.active = true " +
           "AND dl.timestamp >= :minTimestamp " +
           "AND dl.latitude BETWEEN :minLat AND :maxLat " +
           "AND dl.longitude BETWEEN :minLon AND :maxLon")
    List<DriverLocation> findActiveLocationsInBoundingBox(
            @Param("minTimestamp") LocalDateTime minTimestamp,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon);
}
