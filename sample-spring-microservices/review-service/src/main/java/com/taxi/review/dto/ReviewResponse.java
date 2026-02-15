package com.taxi.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long tripId;
    private Long userId;
    private Long driverId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
