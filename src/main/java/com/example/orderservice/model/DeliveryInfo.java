package com.example.orderservice.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryInfo {
    @NotBlank
    private String address;
    @NotBlank
    private String city;
    @NotBlank
    private String postalCode;
    @NotBlank
    private String country;
}