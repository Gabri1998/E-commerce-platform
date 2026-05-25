package com.ecommerce.order.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Table("order_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    @PrimaryKey
    private UUID eventId;
    private String orderId;
    private String eventType; // ORDER_CREATED, ORDER_UPDATED, ORDER_CANCELLED
    private String payload;
    private Instant timestamp;
}