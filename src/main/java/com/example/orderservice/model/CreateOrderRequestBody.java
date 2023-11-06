package com.example.orderservice.model;

import jakarta.validation.Valid;

public record CreateOrderRequestBody(@Valid CustomerInfo customerInfo, @Valid DeliveryInfo deliveryInfo) {
}
