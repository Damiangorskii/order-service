package com.example.orderservice.api;


import com.example.orderservice.model.Order;
import com.example.orderservice.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/order")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("{cartId}")
    public Mono<Order> createOrder(final @PathVariable UUID cartId) {
        return orderService.createOrder(cartId);
    }
}
