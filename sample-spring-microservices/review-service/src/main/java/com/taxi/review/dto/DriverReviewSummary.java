package com.taxi.review.dto;

public class DriverReviewSummary {
    private Long driverId;
    private Double averageRating;
    private Long totalReviews;

    public DriverReviewSummary() {
    }

    public DriverReviewSummary(Long driverId, Double averageRating, Long totalReviews) {
        this.driverId = driverId;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }
}
