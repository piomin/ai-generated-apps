package com.taxi.review.service;

import com.taxi.review.dto.DriverReviewSummary;
import com.taxi.review.dto.PageResponse;
import com.taxi.review.dto.ReviewRequest;
import com.taxi.review.dto.ReviewResponse;
import com.taxi.review.model.Review;
import com.taxi.review.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public ReviewResponse createReview(ReviewRequest request) {
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
        if (page < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }
        if (size == null) {
            size = DEFAULT_PAGE_SIZE;
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Size must not exceed " + MAX_PAGE_SIZE);
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewPage = reviewRepository.findByDriverId(driverId, pageable);

        return new PageResponse<>(
                reviewPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()),
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages()
        );
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
