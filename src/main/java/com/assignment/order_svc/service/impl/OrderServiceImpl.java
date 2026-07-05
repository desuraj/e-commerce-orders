package com.assignment.order_svc.service.impl;

import com.assignment.order_svc.dto.CreateOrderRequest;
import com.assignment.order_svc.dto.OrderResponse;
import com.assignment.order_svc.service.OrderService;
import com.assignment.order_svc.strategy.OrderStrategy;
import com.assignment.order_svc.strategy.OrderStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Orchestrates order creation.
 * Contains zero order-type-specific if/else logic — delegates entirely to
 * the strategy resolved by {@link OrderStrategyFactory}.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderStrategyFactory orderStrategyFactory;

    public OrderServiceImpl(OrderStrategyFactory orderStrategyFactory) {
        this.orderStrategyFactory = orderStrategyFactory;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("createOrder — customer: {}, orderType: {}",
                request.getCustomerId(), request.getOrderType());

        OrderStrategy strategy = orderStrategyFactory.getStrategy(request.getOrderType());
        return strategy.createOrder(request);
    }
}
