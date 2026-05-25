package com.ecommerce.order.service;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderEvent;
import com.ecommerce.order.producer.OrderEventProducer;
import com.ecommerce.order.repository.OrderEventRepository;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventRepository eventRepository;
    private final OrderEventProducer eventProducer;

    @Transactional
    public Order createOrder(Order order) {
        Order saved = orderRepository.save(order);
        // Save event to Cassandra
        OrderEvent event = new OrderEvent(
            UUID.randomUUID(),
            saved.getId(),
            "ORDER_CREATED",
            "Order created with total " + saved.getTotalAmount(),
            Instant.now()
        );
        eventRepository.save(event);
        // Publish Kafka event
        eventProducer.sendOrderCreated(saved);
        return saved;
    }

    public Order getOrder(String id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }
}