package com.example.orderservice.repository;

import com.example.orderservice.model.Order;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReactiveOrderRepositoryAdapter {

    private final OrderRepository orderRepository;

    public Mono<Order> findOrderByOrderId(UUID id) {
        return Mono.fromCallable(() -> orderRepository.findOrderByOrderId(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> deleteOrderByOrderId(UUID id) {
        return Mono.fromRunnable(() -> orderRepository.deleteOrderByOrderId(id))
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Void> deleteByInsertDateTimeBefore(LocalDateTime time) {
        return Mono.fromRunnable(() -> orderRepository.deleteByInsertDateTimeBefore(time))
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Order> save(final Order order) {
        return Mono.fromCallable(() -> orderRepository.save(order))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
