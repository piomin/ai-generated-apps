package com.taxi.review.controller;

import com.taxi.review.dto.DriverReviewSummary;
import com.taxi.review.dto.PageResponse;
import com.taxi.review.dto.ReviewRequest;
import com.taxi.review.dto.ReviewResponse;
import com.taxi.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PageResponse<ReviewResponse>> getDriverReviews(
            @PathVariable Long driverId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PageResponse<ReviewResponse> reviews = reviewService.getDriverReviews(driverId, page, size);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/driver/{driverId}/summary")
    public ResponseEntity<DriverReviewSummary> getDriverReviewSummary(@PathVariable Long driverId) {
        DriverReviewSummary summary = reviewService.getDriverReviewSummary(driverId);
        return ResponseEntity.ok(summary);
    }
}
