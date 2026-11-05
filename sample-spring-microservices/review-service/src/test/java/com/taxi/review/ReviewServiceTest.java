package com.taxi.review;

import com.taxi.review.dto.PageResponse;
import com.taxi.review.dto.ReviewRequest;
import com.taxi.review.model.Review;
import com.taxi.review.repository.ReviewRepository;
import com.taxi.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
    }

    @Test
    void testGetDriverReviewsDefaultPagination() {
        Long driverId = 1L;
        for (int i = 0; i < 25; i++) {
            Review review = new Review();
            review.setTripId(100L + i);
            review.setUserId(10L);
            review.setDriverId(driverId);
            review.setRating(5);
            review.setComment("Review " + i);
            review.setCreatedAt(LocalDateTime.now().minusDays(i));
            reviewRepository.save(review);
        }

        PageResponse<com.taxi.review.dto.ReviewResponse> result = reviewService.getDriverReviews(driverId, null, null);

        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
        assertEquals(25, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(20, result.getContent().size());
    }

    @Test
    void testGetDriverReviewsCustomPagination() {
        Long driverId = 2L;
        for (int i = 0; i < 50; i++) {
            Review review = new Review();
            review.setTripId(200L + i);
            review.setUserId(20L);
            review.setDriverId(driverId);
            review.setRating(4);
            review.setComment("Review " + i);
            review.setCreatedAt(LocalDateTime.now().minusDays(i));
            reviewRepository.save(review);
        }

        PageResponse<com.taxi.review.dto.ReviewResponse> result = reviewService.getDriverReviews(driverId, 1, 10);

        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(50, result.getTotalElements());
        assertEquals(5, result.getTotalPages());
        assertEquals(10, result.getContent().size());
    }

    @Test
    void testGetDriverReviewsEmptyPage() {
        Long driverId = 3L;
        PageResponse<com.taxi.review.dto.ReviewResponse> result = reviewService.getDriverReviews(driverId, 0, 20);

        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void testGetDriverReviewsSizeCappedAtMax() {
        Long driverId = 4L;
        for (int i = 0; i < 150; i++) {
            Review review = new Review();
            review.setTripId(300L + i);
            review.setUserId(30L);
            review.setDriverId(driverId);
            review.setRating(3);
            review.setComment("Review " + i);
            review.setCreatedAt(LocalDateTime.now().minusDays(i));
            reviewRepository.save(review);
        }

        PageResponse<com.taxi.review.dto.ReviewResponse> result = reviewService.getDriverReviews(driverId, 0, 200);

        assertEquals(200, result.getSize());
        assertEquals(100, result.getContent().size());
    }

    @Test
    void testGetDriverReviewsNegativePage() {
        Long driverId = 5L;
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.getDriverReviews(driverId, -1, 20);
        });
    }

    @Test
    void testGetDriverReviewsNonPositiveSize() {
        Long driverId = 6L;
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.getDriverReviews(driverId, 0, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.getDriverReviews(driverId, 0, -5);
        });
    }

    @Test
    void testGetDriverReviewsSortOrder() {
        Long driverId = 7L;
        for (int i = 0; i < 10; i++) {
            Review review = new Review();
            review.setTripId(400L + i);
            review.setUserId(40L);
            review.setDriverId(driverId);
            review.setRating(5);
            review.setComment("Review " + i);
            review.setCreatedAt(LocalDateTime.now().minusDays(i));
            reviewRepository.save(review);
        }

        PageResponse<com.taxi.review.dto.ReviewResponse> result = reviewService.getDriverReviews(driverId, 0, 20);

        List<com.taxi.review.dto.ReviewResponse> content = result.getContent();
        for (int i = 0; i < content.size() - 1; i++) {
            assertTrue(content.get(i).getCreatedAt().isAfter(content.get(i + 1).getCreatedAt()));
        }
    }
}
