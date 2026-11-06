package com.taxi.review.service;

import com.taxi.review.dto.PageResponse;
import com.taxi.review.dto.ReviewRequest;
import com.taxi.review.dto.ReviewResponse;
import com.taxi.review.model.Review;
import com.taxi.review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void shouldGetDefaultPageWhenNoParameters() {
        createTestReviews(25);

        PageResponse<ReviewResponse> response = reviewService.getDriverReviews(1L, null, null);

        assertNotNull(response);
        assertEquals(0, response.getPage());
        assertEquals(20, response.getSize());
        assertEquals(25, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertEquals(20, response.getContent().size());
    }

    @Test
    void shouldGetCustomPageAndSize() {
        createTestReviews(50);

        PageResponse<ReviewResponse> response = reviewService.getDriverReviews(1L, 1, 10);

        assertNotNull(response);
        assertEquals(1, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(50, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
        assertEquals(10, response.getContent().size());
    }

    @Test
    void shouldReturnEmptyPageWhenNoReviews() {
        PageResponse<ReviewResponse> response = reviewService.getDriverReviews(999L, 0, 20);

        assertNotNull(response);
        assertEquals(0, response.getPage());
        assertEquals(20, response.getSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    void shouldCapSizeToMaxWhenExceedingLimit() {
        createTestReviews(200);

        PageResponse<ReviewResponse> response = reviewService.getDriverReviews(1L, 0, 200);

        assertNotNull(response);
        assertEquals(100, response.getSize());
        assertEquals(200, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertEquals(100, response.getContent().size());
    }

    @Test
    void shouldThrowExceptionForNegativePage() {
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.getDriverReviews(1L, -1, 20);
        });
    }

    @Test
    void shouldThrowExceptionForNonPositiveSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.getDriverReviews(1L, 0, 0);
        });
    }

    @Test
    void shouldThrowExceptionForSizeExceedingMax() {
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.getDriverReviews(1L, 0, 101);
        });
    }

    private void createTestReviews(int count) {
        for (int i = 0; i < count; i++) {
            ReviewRequest request = new ReviewRequest(
                    (long) (i + 1),
                    1L,
                    1L,
                    5,
                    "Test review " + i
            );
            reviewService.createReview(request);
        }
    }
}
