package com.taxi.review.service;

import com.taxi.review.dto.DriverReviewSummary;
import com.taxi.review.dto.ReviewRequest;
import com.taxi.review.dto.ReviewResponse;
import com.taxi.review.model.Review;
import com.taxi.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        // Check if review already exists for this trip
        if (reviewRepository.findByTripId(request.getTripId()).isPresent()) {
            throw new RuntimeException("Review already exists for this trip");
        }

        Review review = new Review();
        review.setTripId(request.getTripId());
        review.setUserId(request.getUserId());
        review.setDriverId(request.getDriverId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);

        return mapToResponse(review);
    }

    public List<ReviewResponse> getDriverReviews(Long driverId) {
        return reviewRepository.findByDriverIdOrderByCreatedAtDesc(driverId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DriverReviewSummary getDriverReviewSummary(Long driverId) {
        Double averageRating = reviewRepository.getAverageRatingForDriver(driverId);
        Long totalReviews = reviewRepository.getReviewCountForDriver(driverId);

        return new DriverReviewSummary(
                driverId,
                averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0,
                totalReviews
        );
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setTripId(review.getTripId());
        response.setUserId(review.getUserId());
        response.setDriverId(review.getDriverId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
