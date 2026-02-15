package com.taxi.review.repository;

import com.taxi.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByTripId(Long tripId);
    List<Review> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.driverId = :driverId")
    Double getAverageRatingForDriver(Long driverId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.driverId = :driverId")
    Long getReviewCountForDriver(Long driverId);
}
