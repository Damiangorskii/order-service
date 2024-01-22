package com.example.orderservice.repository;

import com.example.orderservice.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends MongoRepository<Order, String> {

    Optional<Order> findOrderByOrderId(UUID orderId);

    void deleteOrderByOrderId(UUID orderId);

    void deleteByInsertDateTimeBefore(LocalDateTime time);
}
