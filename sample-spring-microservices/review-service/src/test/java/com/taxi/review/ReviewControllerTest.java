package com.taxi.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxi.review.dto.ReviewRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ReviewControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://mock-issuer");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> "http://mock-issuer/jwks");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldCreateReview() throws Exception {
        ReviewRequest request = new ReviewRequest(1L, 1L, 1L, 5, "Great driver!");

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great driver!"));
    }

    @Test
    @WithMockUser
    void shouldGetDriverReviewSummary() throws Exception {
        ReviewRequest request = new ReviewRequest(2L, 1L, 1L, 4, "Good service");
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reviews/driver/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverId").value(1))
                .andExpect(jsonPath("$.totalReviews").exists())
                .andExpect(jsonPath("$.averageRating").exists());
    }

    @Test
    @WithMockUser
    void shouldGetDriverReviewsWithDefaultPagination() throws Exception {
        ReviewRequest request = new ReviewRequest(10L, 1L, 1L, 5, "Great driver!");
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reviews/driver/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser
    void shouldGetDriverReviewsWithCustomPagination() throws Exception {
        for (int i = 1; i <= 35; i++) {
            ReviewRequest req = new ReviewRequest(100L + i, 1L, 2L, 5, "Review " + i);
            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/reviews/driver/2?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(35))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.content").length(10));
    }

    @Test
    @WithMockUser
    void shouldGetEmptyDriverReviews() throws Exception {
        mockMvc.perform(get("/api/reviews/driver/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @WithMockUser
    void shouldCapSizeWhenExceedsMaximum() throws Exception {
        ReviewRequest request = new ReviewRequest(50L, 1L, 3L, 5, "Test review");
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reviews/driver/3?page=0&size=200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    @WithMockUser
    void shouldReturn400ForNegativePage() throws Exception {
        mockMvc.perform(get("/api/reviews/driver/1?page=-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn400ForNonPositiveSize() throws Exception {
        mockMvc.perform(get("/api/reviews/driver/1?page=0&size=0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/reviews/driver/1?page=0&size=-5"))
                .andExpect(status().isBadRequest());
    }
}
