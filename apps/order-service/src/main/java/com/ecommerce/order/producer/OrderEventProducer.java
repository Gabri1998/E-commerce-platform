package com.ecommerce.order.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.order.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendOrderCreated(Order order) {
        try {
            String message = objectMapper.writeValueAsString(order);
            kafkaTemplate.send("order.created", order.getId(), message);
            log.info("Order created event sent: {}", order.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order", e);
        }
    }
}