package com.taxi.review.controller;

import com.taxi.review.dto.DriverReviewSummary;
import com.taxi.review.dto.PageResponse;
import com.taxi.review.dto.ReviewRequest;
import com.taxi.review.dto.ReviewResponse;
import com.taxi.review.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Validated
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Validated @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<PageResponse<ReviewResponse>> getDriverReviews(
            @PathVariable Long driverId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        PageResponse<ReviewResponse> response = reviewService.getDriverReviews(driverId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/driver/{driverId}/summary")
    public ResponseEntity<DriverReviewSummary> getDriverReviewSummary(@PathVariable Long driverId) {
        DriverReviewSummary summary = reviewService.getDriverReviewSummary(driverId);
        return ResponseEntity.ok(summary);
    }
}
