package com.example.orderservice.service;

import com.example.orderservice.client.ShoppingClient;
import com.example.orderservice.model.CustomerInfo;
import com.example.orderservice.model.DeliveryInfo;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

    public static final String ORDER_NOT_FOUND = "Order not found";
    private final OrderRepository orderRepository;
    private final ShoppingClient shoppingClient;

    public Mono<Order> createOrder(final UUID cartId, final CustomerInfo customerInfo, final DeliveryInfo deliveryInfo) {
        return shoppingClient.getShoppingCart(cartId)
                .map(shoppingCart -> new Order(
                        UUID.randomUUID(),
                        shoppingCart.getProducts(),
                        customerInfo,
                        deliveryInfo,
                        false
                ))
                .flatMap(orderRepository::save);
    }

    public Mono<Order> retrieveOrder(final UUID orderId) {
        return orderRepository.findOrderByOrderId(orderId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, ORDER_NOT_FOUND))));
    }

    public Mono<Void> deleteOrder(final UUID orderId) {
        return orderRepository.findOrderByOrderId(orderId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, ORDER_NOT_FOUND))))
                .flatMap(order -> orderRepository.deleteOrderByOrderId(order.getOrderId()));
    }

    public Mono<Order> finalizeOrder(final UUID orderId) {
        return orderRepository.findOrderByOrderId(orderId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, ORDER_NOT_FOUND))))
                .map(this::payForOrder)
                .flatMap(orderRepository::save);
    }

    private Order payForOrder(final Order order) {
        if (!order.isPaid()) {
            order.setPaid(true);
        }
        return order;
    }
}
