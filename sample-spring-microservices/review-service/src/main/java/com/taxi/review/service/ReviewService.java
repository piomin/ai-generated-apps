package com.taxi.review.service;

import com.taxi.review.dto.DriverReviewSummary;
import com.taxi.review.dto.PageResponse;
import com.taxi.review.dto.ReviewRequest;
import com.taxi.review.dto.ReviewResponse;
import com.taxi.review.model.Review;
import com.taxi.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

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

    public PageResponse<ReviewResponse> getDriverReviews(Long driverId, Integer page, Integer size) {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findByDriverId(driverId, pageable);

        List<ReviewResponse> content = reviewPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(content, reviewPage.getNumber(), reviewPage.getSize(),
                reviewPage.getTotalElements(), reviewPage.getTotalPages());
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
