package com.assignment.order_svc.strategy;

import com.assignment.order_svc.dto.CreateOrderRequest;
import com.assignment.order_svc.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Strategy implementation for STANDARD order creation.
 *
 * Flow:
 *   1. Reserve Inventory   (InventoryService)
 *   2. Process Payment     (PaymentService)
 *   3. Persist Order       (OrderRepository)
 *   4. Publish Kafka Event (OrderCreated)
 */
@Component
public class StandardOrderStrategy implements OrderStrategy {

    private static final Logger log = LoggerFactory.getLogger(StandardOrderStrategy.class);

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Processing STANDARD order for customer: {}", request.getCustomerId());

        // TODO Step 1 — Reserve Inventory
        // inventoryService.reserve(request.getItems());

        // TODO Step 2 — Process Payment
        // paymentService.process(request.getCustomerId(), request.getPaymentMode(), total);

        // TODO Step 3 — Persist Order
        // Order order = buildOrder(request);
        // orderRepository.save(order);

        // TODO Step 4 — Publish Kafka Event
        // kafkaPublisher.publish(new OrderCreatedEvent(order));

        // TODO Return populated OrderResponse
        return new OrderResponse();
    }
}
