package com.assignment.order_svc.strategy;

import com.assignment.order_svc.dto.CreateOrderRequest;
import com.assignment.order_svc.dto.OrderResponse;

/**
 * Strategy contract for order creation.
 * Each implementation handles the end-to-end flow for one order type
 * (STANDARD or RUSH) without any cross-type conditional logic.
 */
public interface OrderStrategy {

    /**
     * Creates an order according to this strategy's flow.
     *
     * @param request the create-order request
     * @return the created order response
     */
    OrderResponse createOrder(CreateOrderRequest request);
}
