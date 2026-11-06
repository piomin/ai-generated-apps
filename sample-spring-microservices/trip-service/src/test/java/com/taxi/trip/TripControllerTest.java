package com.taxi.trip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxi.trip.dto.TripRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TripControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://mock-issuer");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> "http://mock-issuer/jwks");
        registry.add("taxi.cancellation.grace-period", () -> "2m");
        registry.add("taxi.cancellation.fee", () -> "5.00");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long tripId;

    @BeforeEach
    void setup() throws Exception {
        TripRequest request = new TripRequest(
                1L,
                "123 Main St",
                40.7128,
                -74.0060,
                "456 Park Ave",
                40.7580,
                -73.9855
        );
        String response = mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        tripId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRequestTrip() throws Exception {
        TripRequest request = new TripRequest(
                1L,
                "123 Main St",
                40.7128,
                -74.0060,
                "456 Park Ave",
                40.7580,
                -73.9855
        );

        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.pickupLocation").value("123 Main St"))
                .andExpect(jsonPath("$.estimatedCost").exists())
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldCancelTripInRequestedStatus() throws Exception {
        mockMvc.perform(put("/api/trips/{tripId}/cancel", tripId)
                .param("cancellationReason", "Changed my mind")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").isNotEmpty())
                .andExpect(jsonPath("$.cancelledBy").value("USER"))
                .andExpect(jsonPath("$.cancellationReason").value("Changed my mind"))
                .andExpect(jsonPath("$.cancellationFee").value(0));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldCancelTripWithDriver() throws Exception {
        mockMvc.perform(put("/api/trips/{tripId}/accept", tripId)
                .param("driverId", "100L"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/trips/{tripId}/cancel", tripId)
                .param("cancellationReason", "Driver cannot make it")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledBy").value("DRIVER"))
                .andExpect(jsonPath("$.driverId").value(null));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldNotCancelTripInProgress() throws Exception {
        mockMvc.perform(put("/api/trips/{tripId}/accept", tripId)
                .param("driverId", "100L"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/trips/{tripId}/start", tripId))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/trips/{tripId}/cancel", tripId)
                .param("cancellationReason", "Cannot cancel")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldNotCancelAlreadyCompletedTrip() throws Exception {
        mockMvc.perform(put("/api/trips/{tripId}/accept", tripId)
                .param("driverId", "100L"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/trips/{tripId}/start", tripId))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/trips/{tripId}/complete", tripId)
                .param("userEmail", "user@example.com"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/trips/{tripId}/cancel", tripId)
                .param("cancellationReason", "Too late")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldNotCancelAlreadyCancelledTrip() throws Exception {
        mockMvc.perform(put("/api/trips/{tripId}/cancel", tripId)
                .param("cancellationReason", "First cancel")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/trips/{tripId}/cancel", tripId)
                .param("cancellationReason", "Already cancelled")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}
