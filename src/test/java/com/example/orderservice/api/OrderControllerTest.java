package com.example.orderservice.api;

import com.example.orderservice.model.*;
import com.example.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    private static final CustomerInfo CUSTOMER_INFO = CustomerInfo.builder()
            .firstName("Joe")
            .lastName("Doe")
            .email("joedoe@test.com")
            .phoneNumber("555666777")
            .build();

    private static final CustomerInfo INVALID_CUSTOMER_INFO = CustomerInfo.builder()
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

    private static final DeliveryInfo INVALID_DELIVERY_INFO = DeliveryInfo.builder()
            .address("Street 1")
            .city("London")
            .postalCode("33333")
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
    private static final Mono<Order> ERROR = Mono.error(new RuntimeException("Some error"));
    private static final String NOT_UUID_STRING = "not-uuid-string";
    public static final PaymentRequest PAYMENT_REQUEST = new PaymentRequest("41111111111111111", "06", "25", "123", "Joe Doe");
    public static final PaymentRequest INVALID_PAYMENT_REQUEST = new PaymentRequest(null, "06", "25", "123", "Joe Doe");

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(orderController).build();
    }

    @Test
    void should_return_created_order() {
        when(orderService.createOrder(any(), any(), any()))
                .thenReturn(Mono.just(ORDER));

        webTestClient.post().uri("/order/{orderId}", ORDER.getOrderId())
                .bodyValue(new CreateOrderRequestBody(CUSTOMER_INFO, DELIVERY_INFO))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Order.class);
    }

    @Test
    void should_return_error_for_wrong_url() {
        webTestClient.post().uri("/order/{orderId}/test", ORDER.getOrderId())
                .bodyValue(new CreateOrderRequestBody(CUSTOMER_INFO, DELIVERY_INFO))
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void should_return_bad_request_for_invalid_customer_info() {
        webTestClient.post().uri("/order/{orderId}", ORDER.getOrderId())
                .bodyValue(new CreateOrderRequestBody(INVALID_CUSTOMER_INFO, DELIVERY_INFO))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void should_return_bad_request_for_invalid_delivery_info() {
        webTestClient.post().uri("/order/{orderId}", ORDER.getOrderId())
                .bodyValue(new CreateOrderRequestBody(CUSTOMER_INFO, INVALID_DELIVERY_INFO))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void should_return_bad_request_for_invalid_orderId_format() {
        webTestClient.post().uri("/order/{orderId}", NOT_UUID_STRING)
                .bodyValue(new CreateOrderRequestBody(CUSTOMER_INFO, DELIVERY_INFO))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void should_return_error_in_case_service_returned_error() {
        when(orderService.createOrder(any(), any(), any()))
                .thenReturn(ERROR);

        webTestClient.post().uri("/order/{orderId}", ORDER.getOrderId())
                .bodyValue(new CreateOrderRequestBody(CUSTOMER_INFO, DELIVERY_INFO))
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    void should_return_order() {
        when(orderService.retrieveOrder(any()))
                .thenReturn(Mono.just(ORDER));

        webTestClient.get().uri("/order/{orderId}", ORDER.getOrderId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Order.class);
    }

    @Test
    void should_return_bad_request_for_invalid_orderId_format_for_retrieve() {
        webTestClient.get().uri("/order/{orderId}", NOT_UUID_STRING)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void should_return_error_in_case_retrieve_error_returned_one() {
        when(orderService.retrieveOrder(any()))
                .thenReturn(ERROR);

        webTestClient.get().uri("/order/{orderId}", ORDER.getOrderId())
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    void should_return_empty_for_deleted_order() {
        when(orderService.deleteOrder(any()))
                .thenReturn(Mono.empty());

        webTestClient.delete().uri("/order/{orderId}", ORDER.getOrderId())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void should_return_bad_request_for_invalid_orderId_format_for_deletion() {
        webTestClient.delete().uri("/order/{orderId}", NOT_UUID_STRING)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void should_return_error_in_case_service_deletion_returned_error() {
        when(orderService.deleteOrder(any()))
                .thenReturn(ERROR.then());

        webTestClient.delete().uri("/order/{orderId}", ORDER.getOrderId())
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    void should_return_finalized_order() {
        when(orderService.finalizeOrder(any()))
                .thenReturn(Mono.just(ORDER));

        webTestClient.post().uri("/order/{orderId}/finalize", ORDER.getOrderId())
                .bodyValue(PAYMENT_REQUEST)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Order.class);
    }

    @Test
    void should_return_bad_request_for_invalid_request_body() {
        webTestClient.post().uri("/order/{orderId}/finalize", ORDER.getOrderId())
                .bodyValue(INVALID_PAYMENT_REQUEST)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void should_return_bad_request_for_invalid_orderId_for_finalize() {
        webTestClient.post().uri("/order/{orderId}/finalize", NOT_UUID_STRING)
                .bodyValue(PAYMENT_REQUEST)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void should_return_error_if_finalize_returned_error() {
        when(orderService.finalizeOrder(any()))
                .thenReturn(ERROR);

        webTestClient.post().uri("/order/{orderId}/finalize", ORDER.getOrderId())
                .bodyValue(PAYMENT_REQUEST)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

}