package com.taxi.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
public class PaymentCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Tokenized card reference from payment provider (e.g., Stripe token)
     * NEVER store full PAN in plaintext - use payment provider tokenization
     */
    @Column(nullable = false)
    private String cardToken;

    /**
     * Last 4 digits of card for display purposes only
     */
    @Column(nullable = false, length = 4)
    private String last4;

    @Column(nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    private String expiryMonth;

    @Column(nullable = false)
    private String expiryYear;

    /**
     * Card brand (e.g., Visa, Mastercard) for display
     */
    @Column(nullable = true)
    private String cardBrand;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
