package com.example.orderservice.service;


import com.example.orderservice.client.ShoppingClient;
import com.example.orderservice.model.*;
import com.example.orderservice.repository.ReactiveOrderRepositoryAdapter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private static final CustomerInfo CUSTOMER_INFO = CustomerInfo.builder()
            .firstName("Joe")
            .lastName("Doe")
            .email("joedoe@test.com")
            .phoneNumber("555666777")
            .build();
    private static final DeliveryInfo DELIVERY_INFO = DeliveryInfo.builder()
            .address("Street 1")
            .city("London")
            .postalCode("33333")
            .country("United Kingdom")
            .build();

    private static final Order ORDER = Order.builder()
            .orderId(UUID.randomUUID())
            .products(List.of(Product.builder()
                    .id(UUID.randomUUID())
                    .name("Test product")
                    .description("Test description")
                    .price(BigDecimal.TEN)
                    .manufacturer(Manufacturer.builder()
                            .id(UUID.randomUUID())
                            .name("manufacturer name")
                            .address("address")
                            .contact("contact")
                            .build())
                    .categories(List.of(Category.BABY_PRODUCTS))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .reviews(List.of(Review.builder()
                            .reviewerName("Name")
                            .comment("Comment")
                            .rating(5)
                            .reviewDate(LocalDateTime.now())
                            .build()))
                    .build()))
            .customerInfo(CUSTOMER_INFO)
            .deliveryInfo(DELIVERY_INFO)
            .isPaid(false)
            .build();

    private static final Order FINALIZED_ORDER = Order.builder()
            .orderId(UUID.randomUUID())
            .products(List.of(Product.builder()
                    .id(UUID.randomUUID())
                    .name("Test product")
                    .description("Test description")
                    .price(BigDecimal.TEN)
                    .manufacturer(Manufacturer.builder()
                            .id(UUID.randomUUID())
                            .name("manufacturer name")
                            .address("address")
                            .contact("contact")
                            .build())
                    .categories(List.of(Category.BABY_PRODUCTS))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .reviews(List.of(Review.builder()
                            .reviewerName("Name")
                            .comment("Comment")
                            .rating(5)
                            .reviewDate(LocalDateTime.now())
                            .build()))
                    .build()))
            .customerInfo(CUSTOMER_INFO)
            .deliveryInfo(DELIVERY_INFO)
            .isPaid(true)
            .build();

    private static final ShoppingCart SHOPPING_CART = ShoppingCart.builder()
            .id(UUID.randomUUID())
            .products(List.of(Product.builder()
                    .id(UUID.randomUUID())
                    .name("Test product")
                    .description("Test description")
                    .price(BigDecimal.TEN)
                    .manufacturer(Manufacturer.builder()
                            .id(UUID.randomUUID())
                            .name("manufacturer name")
                            .address("address")
                            .contact("contact")
                            .build())
                    .categories(List.of(Category.BABY_PRODUCTS))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .reviews(List.of(Review.builder()
                            .reviewerName("Name")
                            .comment("Comment")
                            .rating(5)
                            .reviewDate(LocalDateTime.now())
                            .build()))
                    .build()))
            .build();

    @Mock
    private ReactiveOrderRepositoryAdapter orderRepository;

    @Mock
    private ShoppingClient shoppingClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FilePart filePart;

    private OrderService orderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(orderRepository, shoppingClient, objectMapper);
    }

    @Test
    void should_return_order() {
        when(orderRepository.findOrderByOrderId(ORDER.getOrderId())).thenReturn(Mono.just(ORDER));

        orderService.retrieveOrder(ORDER.getOrderId())
                .as(StepVerifier::create)
                .expectNext(ORDER)
                .expectComplete()
                .verify();
    }

    @Test
    void should_not_return_order_if_it_does_not_exist() {
        when(orderRepository.findOrderByOrderId(ORDER.getOrderId())).thenReturn(Mono.empty());

        orderService.retrieveOrder(ORDER.getOrderId())
                .as(StepVerifier::create)
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(ResponseStatusException.class)
                            .hasMessage("404 NOT_FOUND \"Order not found\"");
                })
                .verify();
    }

    @Test
    void should_create_order() {
        UUID cartId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(shoppingClient.getShoppingCart(cartId)).thenReturn(Mono.just(SHOPPING_CART));
        when(orderRepository.save(any()))
                .thenReturn(Mono.just(Order.builder()
                        .orderId(orderId)
                        .products(SHOPPING_CART.getProducts())
                        .deliveryInfo(DELIVERY_INFO)
                        .customerInfo(CUSTOMER_INFO)
                        .isPaid(false)
                        .build())
                );

        orderService.createOrder(cartId, CUSTOMER_INFO, DELIVERY_INFO)
                .as(StepVerifier::create)
                .expectNextMatches(order -> {
                    assertThat(order.getOrderId()).isNotNull();
                    assertThat(order.getProducts()).containsExactlyInAnyOrderElementsOf(SHOPPING_CART.getProducts());
                    assertThat(order.getCustomerInfo()).isEqualTo(CUSTOMER_INFO);
                    assertThat(order.getDeliveryInfo()).isEqualTo(DELIVERY_INFO);
                    assertThat(order.isPaid()).isFalse();
                    return true;
                })
                .expectComplete()
                .verify();
    }

    @Test
    void should_return_error_if_fetching_cart_returned_error() {
        when(shoppingClient.getShoppingCart(ORDER.getOrderId()))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found")));

        orderService.createOrder(ORDER.getOrderId(), CUSTOMER_INFO, DELIVERY_INFO)
                .as(StepVerifier::create)
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(ResponseStatusException.class)
                            .hasMessage("404 NOT_FOUND \"Cart not found\"");
                })
                .verify();
    }

    @Test
    void should_delete_order() {
        when(orderRepository.findOrderByOrderId(ORDER.getOrderId())).thenReturn(Mono.just(ORDER));
        when(orderRepository.deleteOrderByOrderId(ORDER.getOrderId())).thenReturn(Mono.empty());

        orderService.deleteOrder(ORDER.getOrderId())
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        verify(orderRepository, times(1)).findOrderByOrderId(ORDER.getOrderId());
        verify(orderRepository, times(1)).deleteOrderByOrderId(ORDER.getOrderId());
    }

    @Test
    void should_return_error_if_order_not_found() {
        when(orderRepository.findOrderByOrderId(ORDER.getOrderId()))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")));

        orderService.deleteOrder(ORDER.getOrderId())
                .as(StepVerifier::create)
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(ResponseStatusException.class)
                            .hasMessage("404 NOT_FOUND \"Order not found\"");
                })
                .verify();

        verify(orderRepository, times(1)).findOrderByOrderId(ORDER.getOrderId());
        verify(orderRepository, never()).deleteOrderByOrderId(ORDER.getOrderId());
    }

    @Test
    void should_return_error_if_deletion_failed() {
        when(orderRepository.findOrderByOrderId(ORDER.getOrderId())).thenReturn(Mono.just(ORDER));
        when(orderRepository.deleteOrderByOrderId(ORDER.getOrderId()))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Some error")));

        orderService.deleteOrder(ORDER.getOrderId())
                .as(StepVerifier::create)
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(ResponseStatusException.class)
                            .hasMessage("500 INTERNAL_SERVER_ERROR \"Some error\"");
                })
                .verify();

        verify(orderRepository, times(1)).findOrderByOrderId(ORDER.getOrderId());
        verify(orderRepository, times(1)).deleteOrderByOrderId(ORDER.getOrderId());
    }

    @Test
    void should_finalize_order() {
        when(orderRepository.findOrderByOrderId(FINALIZED_ORDER.getOrderId())).thenReturn(Mono.just(FINALIZED_ORDER));
        when(orderRepository.save(any())).thenReturn(Mono.just(FINALIZED_ORDER));

        orderService.finalizeOrder(FINALIZED_ORDER.getOrderId())
                .as(StepVerifier::create)
                .expectNextMatches(order -> {
                    assertThat(order.isPaid()).isTrue();
                    return true;
                })
                .expectComplete()
                .verify();
    }

    @Test
    void should_return_error_if_order_not_found_for_finalize() {
        when(orderRepository.findOrderByOrderId(ORDER.getOrderId()))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")));

        orderService.finalizeOrder(ORDER.getOrderId())
                .as(StepVerifier::create)
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(ResponseStatusException.class)
                            .hasMessage("404 NOT_FOUND \"Order not found\"");
                })
                .verify();
    }

    @Test
    void should_return_error_if_updating_order_failed() {
        when(orderRepository.findOrderByOrderId(ORDER.getOrderId())).thenReturn(Mono.just(ORDER));
        when(orderRepository.save(any()))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Some error")));

        orderService.finalizeOrder(ORDER.getOrderId())
                .as(StepVerifier::create)
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(ResponseStatusException.class)
                            .hasMessage("500 INTERNAL_SERVER_ERROR \"Some error\"");
                })
                .verify();
    }

    @Test
    void should_upload_orders() throws Exception {
        String jsonContent = "[{\"orderId\":\"...\", ...}]";
        List<Order> orders = List.of(
                ORDER,
                FINALIZED_ORDER
        );

        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        when(filePart.content()).thenReturn(Flux.just(dataBufferFactory.wrap(jsonContent.getBytes(StandardCharsets.UTF_8))));

        when(objectMapper.readValue(any(byte[].class), any(TypeReference.class))).thenReturn(orders);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        orderService.uploadProducts(filePart)
                .as(StepVerifier::create)
                .expectSubscription()
                .expectNextCount(orders.size())
                .verifyComplete();

        orders.forEach(order -> Mockito.verify(orderRepository).save(order));
    }

}