package com.example.orderservice.service;

import com.example.orderservice.client.ShoppingClient;
import com.example.orderservice.model.CustomerInfo;
import com.example.orderservice.model.DeliveryInfo;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ShoppingClient shoppingClient;

    public Mono<Order> createOrder(final UUID cartId) {
        return shoppingClient.getShoppingCart(cartId)
                .map(shoppingCart -> new Order(
                        UUID.randomUUID(), shoppingCart.getProducts(),
                        CustomerInfo.builder().firstName("Damian").build(),
                        DeliveryInfo.builder().city("Gdańsk").build(),
                        false
                ))
                .flatMap(orderRepository::save);
    }
}
