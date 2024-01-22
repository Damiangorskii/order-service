package com.example.orderservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
@AllArgsConstructor
@Slf4j
public class OrderCleanupScheduler {

    private final OrderService orderService;

    @Scheduled(cron = "0 0/3 * * * *")
    public void cleanUpOldOrders() {
        orderService.deleteOldOrders()
                .doOnSuccess(s -> log.info("Successfully removed old orders"))
                .doOnError(err -> log.error("Error occurred during old orders removal"))
                .subscribe();
    }
}
