package com.example.orderservice.api;


import com.example.orderservice.model.CreateOrderRequestBody;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.PaymentRequest;
import com.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/order")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("{cartId}")
    public Mono<Order> createOrder(final @PathVariable UUID cartId, final @RequestBody @Valid CreateOrderRequestBody requestBody) {
        return orderService.createOrder(cartId, requestBody.customerInfo(), requestBody.deliveryInfo());
    }

    @GetMapping("{orderId}")
    public Mono<Order> retrieveOrder(final @PathVariable UUID orderId) {
        return orderService.retrieveOrder(orderId);
    }

    @DeleteMapping("{orderId}")
    public Mono<Void> deleteOrder(final @PathVariable UUID orderId) {
        return orderService.deleteOrder(orderId);
    }

    @PostMapping("{orderId}/finalize")
    public Mono<Order> finalizeOrder(final @PathVariable UUID orderId, final @RequestBody @Valid PaymentRequest paymentRequest) {
        return orderService.finalizeOrder(orderId);
    }
}
