package com.taxi.user.service;

import com.taxi.user.dto.PaymentCardRequest;
import com.taxi.user.dto.UserRegistrationRequest;
import com.taxi.user.dto.UserResponse;
import com.taxi.user.model.PaymentCard;
import com.taxi.user.model.User;
import com.taxi.user.repository.PaymentCardRepository;
import com.taxi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole("USER");

        user = userRepository.save(user);

        return mapToResponse(user);
    }

    @Transactional
    public void addPaymentCard(Long userId, PaymentCardRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PaymentCard existingCard = paymentCardRepository.findByUserId(userId).orElse(null);
        if (existingCard != null) {
            paymentCardRepository.delete(existingCard);
        }

        PaymentCard card = new PaymentCard();
        card.setUser(user);
        card.setCardNumber(request.getCardNumber());
        card.setCardHolderName(request.getCardHolderName());
        card.setExpiryMonth(request.getExpiryMonth());
        card.setExpiryYear(request.getExpiryYear());
        card.setCvv(request.getCvv());

        paymentCardRepository.save(card);
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setHasPaymentCard(user.getPaymentCard() != null);
        return response;
    }
}
