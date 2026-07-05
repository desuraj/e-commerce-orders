package com.assignment.order_svc.dto;

import com.assignment.order_svc.enums.DeliveryType;
import com.assignment.order_svc.enums.OrderStatus;
import com.assignment.order_svc.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO returned after a successful order creation.
 */
public class OrderResponse {

    private Long id;
    private String customerId;
    private OrderStatus status;
    private OrderType orderType;
    private DeliveryType deliveryType;
    private BigDecimal totalAmount;
    private String estimatedDelivery;
    private LocalDateTime createdAt;

    public OrderResponse() {}

    public Long getId()                              { return id; }
    public void setId(Long v)                        { this.id = v; }

    public String getCustomerId()                    { return customerId; }
    public void setCustomerId(String v)              { this.customerId = v; }

    public OrderStatus getStatus()                   { return status; }
    public void setStatus(OrderStatus v)             { this.status = v; }

    public OrderType getOrderType()                  { return orderType; }
    public void setOrderType(OrderType v)            { this.orderType = v; }

    public DeliveryType getDeliveryType()            { return deliveryType; }
    public void setDeliveryType(DeliveryType v)      { this.deliveryType = v; }

    public BigDecimal getTotalAmount()               { return totalAmount; }
    public void setTotalAmount(BigDecimal v)         { this.totalAmount = v; }

    public String getEstimatedDelivery()             { return estimatedDelivery; }
    public void setEstimatedDelivery(String v)       { this.estimatedDelivery = v; }

    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime v)        { this.createdAt = v; }
}
