package com.taxi.review.controller;

import com.taxi.review.dto.DriverReviewSummary;
import com.taxi.review.dto.ReviewRequest;
import com.taxi.review.dto.ReviewResponse;
import com.taxi.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<ReviewResponse>> getDriverReviews(@PathVariable Long driverId) {
        List<ReviewResponse> reviews = reviewService.getDriverReviews(driverId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/driver/{driverId}/summary")
    public ResponseEntity<DriverReviewSummary> getDriverReviewSummary(@PathVariable Long driverId) {
        DriverReviewSummary summary = reviewService.getDriverReviewSummary(driverId);
        return ResponseEntity.ok(summary);
    }
}
