package com.assignment.order_svc.service;

import com.assignment.order_svc.dto.CreateOrderRequest;
import com.assignment.order_svc.dto.OrderResponse;

/**
 * Service interface for order operations.
 * OrderController depends on this abstraction — never on the concrete impl.
 */
public interface OrderService {

    /**
     * Creates an order by delegating to the strategy matched by {@code orderType}.
     *
     * @param request the full create-order request
     * @return the created order response
     */
    OrderResponse createOrder(CreateOrderRequest request);
}
