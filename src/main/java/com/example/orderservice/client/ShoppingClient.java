package com.example.orderservice.client;

import com.example.orderservice.model.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
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
                .bodyToMono(ShoppingCart.class);
    }
}
