package com.example.orderservice.repository;

import com.example.orderservice.model.Order;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderRepository extends ReactiveMongoRepository<Order, String> {

    Mono<Order> findOrderByOrderId(UUID orderId);

    Mono<Void> deleteOrderByOrderId(UUID orderId);

}
