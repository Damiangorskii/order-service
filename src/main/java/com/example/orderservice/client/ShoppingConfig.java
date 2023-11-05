package com.example.orderservice.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "internal.api.shopping-service")
@Getter
@Setter
public class ShoppingConfig {

    private String url;
}
