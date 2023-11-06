package com.example.orderservice.client;

import com.example.orderservice.model.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ShoppingClient {

    private final WebClient webClient;

    private final ShoppingConfig config;

    @Autowired
    public ShoppingClient(WebClient.Builder webClientBuilder, ShoppingConfig config) {
        this.webClient = WebClient.builder().baseUrl(config.getUrl()).build();
        this.config = config;
    }

    public Mono<ShoppingCart> getShoppingCart(final UUID cartId) {
        return webClient.get()
                .uri("/{cartId}", cartId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found")))
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred")))
                .bodyToMono(ShoppingCart.class);
    }
}
