package com.example.orderservice.service;

import com.example.orderservice.client.ShoppingClient;
import com.example.orderservice.model.CustomerInfo;
import com.example.orderservice.model.DeliveryInfo;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

    public static final String ORDER_NOT_FOUND = "Order not found";
    private final OrderRepository orderRepository;
    private final ShoppingClient shoppingClient;
    private final ObjectMapper objectMapper;

    public Mono<Order> createOrder(final UUID cartId, final CustomerInfo customerInfo, final DeliveryInfo deliveryInfo) {
        return shoppingClient.getShoppingCart(cartId)
                .map(shoppingCart -> new Order(
                        UUID.randomUUID(),
                        shoppingCart.getProducts(),
                        customerInfo,
                        deliveryInfo,
                        false,
                        LocalDateTime.now()
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

    public Flux<Order> uploadProducts(final FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .flatMapMany(dataBuffer -> Flux.using(
                        () -> dataBuffer,
                        buffer -> {
                            byte[] bytes = new byte[buffer.readableByteCount()];
                            buffer.read(bytes);
                            try {
                                return Flux.fromIterable(
                                        objectMapper.readValue(bytes, new TypeReference<List<Order>>() {
                                        })
                                );
                            } catch (Exception e) {
                                return Flux.error(e);
                            }
                        },
                        DataBufferUtils::release
                ))
                .map(this::setOrderIdAndInsertDateTime)
                .flatMap(orderRepository::save);
    }

    public Mono<Void> deleteOldOrders() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(1);
        return orderRepository.deleteByInsertDateTimeBefore(oneMinuteAgo);
    }

    private Order payForOrder(final Order order) {
        if (!order.isPaid()) {
            order.setPaid(true);
        }
        return order;
    }

    private Order setOrderIdAndInsertDateTime(final Order order) {
        order.setOrderId(UUID.randomUUID());
        order.setInsertDateTime(LocalDateTime.now());
        return order;
    }
}
