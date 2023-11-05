package com.example.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInfo {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}
