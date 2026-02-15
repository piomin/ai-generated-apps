package com.taxi.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverReviewSummary {
    private Long driverId;
    private Double averageRating;
    private Long totalReviews;
}
