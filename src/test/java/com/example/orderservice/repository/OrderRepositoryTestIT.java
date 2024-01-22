package com.example.orderservice.repository;

import com.example.orderservice.model.CustomerInfo;
import com.example.orderservice.model.DeliveryInfo;
import com.example.orderservice.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class OrderRepositoryTestIT {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void should_find_order_by_orderId() {
        UUID orderId = UUID.randomUUID();
        orderRepository.save(new Order(orderId, Collections.emptyList(), new CustomerInfo(), new DeliveryInfo(), false, LocalDateTime.now()));

        Optional<Order> foundOrder = orderRepository.findOrderByOrderId(orderId);
        assertTrue(foundOrder.isPresent());
        assertEquals(orderId, foundOrder.get().getOrderId());
    }

    @Test
    void should_delete_order_by_orderId() {
        UUID orderId = UUID.randomUUID();
        orderRepository.save(new Order(orderId, Collections.emptyList(), new CustomerInfo(), new DeliveryInfo(), false, LocalDateTime.now()));

        orderRepository.deleteOrderByOrderId(orderId);

        Optional<Order> foundOrder = orderRepository.findOrderByOrderId(orderId);
        assertFalse(foundOrder.isPresent());
    }

}