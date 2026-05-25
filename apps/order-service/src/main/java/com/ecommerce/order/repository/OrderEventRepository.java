package com.ecommerce.order.repository;

import com.ecommerce.order.model.OrderEvent;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface OrderEventRepository extends CassandraRepository<OrderEvent, UUID> {
}