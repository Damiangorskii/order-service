package com.example.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Manufacturer manufacturer;
    private List<Category> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Review> reviews;
}

