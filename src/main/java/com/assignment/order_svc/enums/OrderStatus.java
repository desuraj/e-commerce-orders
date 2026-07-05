package com.assignment.order_svc.enums;

/**
 * Lifecycle states of an order.
 * Valid transitions: PENDING → PROCESSING → SHIPPED → DELIVERED
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED
}
