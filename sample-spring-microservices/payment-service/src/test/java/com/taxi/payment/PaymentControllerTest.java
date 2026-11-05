package com.taxi.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxi.payment.controller.PaymentController;
import com.taxi.payment.dto.PaymentResponse;
import com.taxi.payment.model.Payment;
import com.taxi.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.taxi.payment.service.PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "123")
    void shouldGetPaymentByTripId() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L, 100L, 123L, new BigDecimal("25.50"),
                Payment.PaymentStatus.COMPLETED, "txn-123", LocalDateTime.now()
        );

        given(paymentService.getPaymentByTripId(eq(100L))).willReturn(response);

        mockMvc.perform(get("/api/payments/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tripId").value(100))
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.amount").value(25.50))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.transactionId").value("txn-123"));
    }

    @Test
    @WithMockUser(username = "123")
    void shouldReturn404WhenPaymentNotFound() throws Exception {
        given(paymentService.getPaymentByTripId(any(Long.class)))
                .willThrow(new RuntimeException("Payment not found for trip: 999"));

        mockMvc.perform(get("/api/payments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/payments/100"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "123")
    void shouldGetUserPaymentHistory() throws Exception {
        PaymentResponse response1 = new PaymentResponse(
                1L, 100L, 123L, new BigDecimal("25.50"),
                Payment.PaymentStatus.COMPLETED, "txn-123", LocalDateTime.now().minusDays(2)
        );
        PaymentResponse response2 = new PaymentResponse(
                2L, 101L, 123L, new BigDecimal("30.00"),
                Payment.PaymentStatus.COMPLETED, "txn-124", LocalDateTime.now().minusDays(1)
        );

        given(paymentService.getPaymentsByUserId(eq(123L)))
                .willReturn(List.of(response2, response1));

        mockMvc.perform(get("/api/payments/user/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1));
    }

    @Test
    @WithMockUser(username = "456")
    void shouldNotAccessAnotherUsersPayments() throws Exception {
        mockMvc.perform(get("/api/payments/user/123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "123")
    void shouldReturnEmptyListWhenNoPayments() throws Exception {
        given(paymentService.getPaymentsByUserId(eq(123L)))
                .willReturn(List.of());

        mockMvc.perform(get("/api/payments/user/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
