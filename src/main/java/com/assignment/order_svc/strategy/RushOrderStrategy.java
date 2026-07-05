package com.assignment.order_svc.strategy;

import com.assignment.order_svc.dto.CreateOrderRequest;
import com.assignment.order_svc.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Strategy implementation for RUSH order creation.
 *
 * Flow:
 *   1. Calculate Order Total
 *   2. Validate Amount >= $100       → 400 if not met
 *   3. Validate Rush Zone            → 400 if ineligible  (DeliveryService)
 *   4. Reserve Inventory             → error if out of stock (InventoryService)
 *   5. Process Payment               → release inventory on failure (PaymentService)
 *   6. Persist Order                 (OrderRepository)
 *   7. Publish Kafka Event           (OrderCreated)
 */
@Component
public class RushOrderStrategy implements OrderStrategy {

    private static final Logger log = LoggerFactory.getLogger(RushOrderStrategy.class);

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Processing RUSH order for customer: {}", request.getCustomerId());

        // TODO Step 1 — Calculate Order Total
        // BigDecimal total = calculateTotal(request.getItems());

        // TODO Step 2 — Validate minimum order value ($100)
        // if (total.compareTo(RUSH_MINIMUM) < 0) {
        //     throw new BusinessException("Minimum order value for Rush Delivery is $100.");
        // }

        // TODO Step 3 — Validate Rush Zone via DeliveryService
        // boolean eligible = deliveryService.isRushZoneEligible(
        //         request.getDeliveryAddress().getPincode());
        // if (!eligible) {
        //     throw new BusinessException("Rush Delivery unavailable for this location.");
        // }

        // TODO Step 4 — Reserve Inventory via InventoryService
        // inventoryService.reserve(request.getItems());

        // TODO Step 5 — Process Payment via PaymentService
        // paymentService.process(request.getCustomerId(), request.getPaymentMode(), total);

        // TODO Step 6 — Persist Order via OrderRepository
        // Order order = buildOrder(request);
        // orderRepository.save(order);

        // TODO Step 7 — Publish Kafka Event
        // kafkaPublisher.publish(new OrderCreatedEvent(order));

        // TODO Return populated OrderResponse
        return new OrderResponse();
    }
}
