package com.example.orderservice.repository;

import com.example.orderservice.model.CustomerInfo;
import com.example.orderservice.model.DeliveryInfo;
import com.example.orderservice.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@DataMongoTest
class OrderRepositoryTestIT {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void should_find_order_by_orderId() {
        UUID orderId = UUID.randomUUID();
        orderRepository.save(new Order(orderId, Collections.emptyList(), new CustomerInfo(), new DeliveryInfo(), false, LocalDateTime.now())).block();

        orderRepository.findOrderByOrderId(orderId)
                .as(StepVerifier::create)
                .expectNextMatches(order -> order.getOrderId().equals(orderId))
                .expectComplete()
                .verify();
    }

    @Test
    void should_delete_order_by_orderId() {
        UUID orderId = UUID.randomUUID();
        orderRepository.save(new Order(orderId, Collections.emptyList(), new CustomerInfo(), new DeliveryInfo(), false, LocalDateTime.now())).block();

        orderRepository.deleteOrderByOrderId(orderId)
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        orderRepository.findOrderByOrderId(orderId)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

}