package com.example.orderservice.model;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(
        @NotBlank
        String cardNumber,
        @NotBlank
        String expirationMonth,
        @NotBlank
        String expirationYear,
        @NotBlank
        String securityCode,
        @NotBlank
        String cardOwner
) {
}
