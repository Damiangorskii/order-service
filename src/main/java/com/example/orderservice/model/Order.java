package com.example.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    private UUID orderId;
    private List<Product> products;
    private CustomerInfo customerInfo;
    private DeliveryInfo deliveryInfo;
    private boolean isPaid;
}
