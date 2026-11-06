package com.taxi.review.dto;

import java.time.LocalDateTime;

public class ReviewRequest {
    private Long tripId;
    private Long userId;
    private Long driverId;
    private Integer rating;
    private String comment;

    public ReviewRequest() {
    }

    public ReviewRequest(Long tripId, Long userId, Long driverId, Integer rating, String comment) {
        this.tripId = tripId;
        this.userId = userId;
        this.driverId = driverId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
