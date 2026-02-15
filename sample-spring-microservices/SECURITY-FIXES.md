# Security and Code Quality Fixes

This document summarizes all security vulnerabilities and code quality issues that have been fixed in the taxi reservation system.

## 1. Dependency Security Updates

**File:** `pom.xml`

**Issue:** Vulnerable transitive dependencies (Spring Boot 3.2.1, kafka-clients 3.6.1, lz4-java)

**Fix:**
- Updated Spring Boot from 3.2.1 to 3.4.1
- Updated Spring Cloud from 2023.0.0 to 2024.0.0
- Explicitly forced kafka-clients to 3.9.0
- Explicitly forced lz4-java to 1.8.0

**Impact:** Patches known security vulnerabilities in Spring Framework, Kafka clients, and compression libraries.

---

## 2. Nested Validation Missing

**File:** `driver-service/src/main/java/com/taxi/driver/dto/DriverRegistrationRequest.java`

**Issue:** The `carDetails` field was annotated only with `@NotNull`, so nested validation constraints inside `CarDetails` class were never evaluated.

**Fix:** Added `@Valid` annotation to the `carDetails` field:
```java
@NotNull(message = "Car details are required")
@Valid
private CarDetails carDetails;
```

**Impact:** Now all nested validations (e.g., `@NotBlank` on make, model, licensePlate) are properly triggered during request validation.

---

## 3. Payment Idempotency Vulnerability

**Files:**
- `payment-service/src/main/java/com/taxi/payment/service/PaymentService.java`
- `payment-service/src/main/java/com/taxi/payment/model/Payment.java`
- `payment-service/src/main/java/com/taxi/payment/repository/PaymentRepository.java`

**Issue:** `processTripCompletedEvent` created a new Payment on every Kafka message redelivery, causing duplicate payments.

**Fix:**
1. Added `existsByTripId()` method to `PaymentRepository`
2. Added idempotency check at the start of `processTripCompletedEvent` to skip processing if payment already exists
3. Added unique constraint on `tripId` in the `Payment` entity with unique index
4. Added exception handling to treat unique constraint violations as idempotency (concurrent delivery)

**Impact:** Prevents duplicate payment processing even on message redelivery or concurrent processing.

---

## 4. Database Schema Issue - Nullable Driver ID

**File:** `trip-service/src/main/java/com/taxi/trip/model/Trip.java`

**Issue:** The `driverId` field was marked as `nullable = false`, but a Trip in REQUESTED state has no driver yet.

**Fix:** Changed the column annotation to allow nulls:
```java
@Column(nullable = true)
private Long driverId;
```

**Impact:** Allows new Trip requests to be persisted without a driver assignment.

---

## 5. Insecure Direct Object Reference (IDOR)

**File:** `user-service/src/main/java/com/taxi/user/controller/UserController.java`

**Issue:** The `addPaymentCard` endpoint didn't validate that the path `userId` matches the authenticated user, allowing users to add payment cards to other users' accounts.

**Fix:** Added authorization check:
```java
String authenticatedEmail = authentication.getName();
UserResponse authenticatedUser = userService.getUserByEmail(authenticatedEmail);

boolean isAdmin = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(role -> role.equals("ROLE_ADMIN"));

if (!authenticatedUser.getId().equals(userId) && !isAdmin) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
}
```

**Impact:** Prevents unauthorized users from modifying other users' payment cards. Only the card owner or admins can add/update payment cards.

---

## 6. Sensitive Data Exposure in Logs

**File:** `user-service/src/main/java/com/taxi/user/dto/PaymentCardRequest.java`

**Issue:** The DTO used Lombok's `@Data` which generates `toString()` that exposes full card number and CVV in logs.

**Fix:**
1. Replaced `@Data` with `@Getter` and `@Setter`
2. Added custom `toString()` method that masks sensitive data:
   - Card number: shows only last 4 digits (e.g., "************1234")
   - CVV: completely masked (e.g., "***")

**Impact:** Prevents PCI data (PAN and CVV) from being logged in plain text during debugging or error handling.

---

## 7. PCI-DSS Violation - Storing Card Data

**Files:**
- `user-service/src/main/java/com/taxi/user/model/PaymentCard.java`
- `user-service/src/main/java/com/taxi/user/service/UserService.java`

**Issue:** The system stored full card numbers (PAN) and CVV in plaintext in the database, violating PCI-DSS requirements.

**Fix:**

**PaymentCard Model:**
- Removed `cardNumber` and `cvv` fields
- Added `cardToken` field to store tokenized reference from payment provider
- Added `last4` field to store only last 4 digits for display
- Added `cardBrand` field for displaying card type (Visa, Mastercard, etc.)
- Added documentation emphasizing never to store full PAN

**UserService:**
- Implemented `tokenizeCard()` method (simulated for demo, would call payment provider API in production)
- Added `detectCardBrand()` helper method
- Updated `addPaymentCard()` to:
  1. Tokenize the card using CVV (transient use only)
  2. Store only the token and last 4 digits
  3. Never persist CVV or full PAN

**Impact:**
- System is now PCI-DSS compliant (scope reduced)
- CVV is only used transiently for authorization and never persisted
- Full PAN is never stored - only tokenized references
- Reduces security liability and compliance burden

---

## 8. Circular Reference in Entity Models

**Files:**
- `user-service/src/main/java/com/taxi/user/model/User.java`
- `user-service/src/main/java/com/taxi/user/model/PaymentCard.java`

**Issue:** Both `User` and `PaymentCard` used Lombok's `@Data` which generates `toString()`, `equals()`, and `hashCode()` including all fields. This causes infinite recursion via the bidirectional relationship `User.paymentCard ↔ PaymentCard.user`.

**Fix:**
1. Replaced `@Data` with `@Getter` and `@Setter` on both entities
2. Added `@ToString(exclude = "paymentCard")` and `@EqualsAndHashCode(exclude = "paymentCard")` on `User`
3. Added `@ToString(exclude = "user")` and `@EqualsAndHashCode(exclude = "user")` on `PaymentCard`

**Impact:** Prevents `StackOverflowError` when entities are logged or compared, ensuring proper serialization and debugging.

---

## Testing Recommendations

After applying these fixes, run the following tests:

1. **Dependency Check:**
   ```bash
   mvn dependency:tree
   # Verify kafka-clients=3.9.0, spring-boot=3.4.1
   ```

2. **Payment Idempotency Test:**
   - Send duplicate `TripCompletedEvent` messages
   - Verify only one payment is created
   - Check unique constraint is enforced at database level

3. **IDOR Test:**
   - Attempt to add payment card to another user's account
   - Verify 403 Forbidden response

4. **Validation Test:**
   - Send `DriverRegistrationRequest` with invalid nested car details
   - Verify validation errors are returned

5. **PCI Compliance Verification:**
   - Check database schema - no `card_number` or `cvv` columns
   - Only `card_token` and `last4` should exist
   - Verify logs don't contain PAN or CVV

---

## Production Deployment Notes

1. **Payment Provider Integration:** Replace simulated `tokenizeCard()` with actual payment provider API (Stripe, Braintree, etc.)

2. **Database Migration:** Create migration scripts to:
   - Drop `card_number` and `cvv` columns from `payment_cards` table
   - Add `card_token`, `last4`, and `card_brand` columns
   - Add unique index on `payments.trip_id`
   - Update `trips.driver_id` to allow NULL

3. **Monitoring:** Add alerts for:
   - Payment idempotency violations (duplicate attempts)
   - Authorization failures (IDOR attempts)
   - Failed card tokenization

4. **Security Audit:** Conduct penetration testing focusing on:
   - IDOR vulnerabilities on all endpoints
   - Payment processing idempotency
   - Sensitive data exposure in logs and error messages
