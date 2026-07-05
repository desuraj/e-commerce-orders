package com.assignment.order_svc.dto;

import com.assignment.order_svc.enums.DeliveryType;
import com.assignment.order_svc.enums.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for creating a new order (STANDARD or RUSH).
 */
public class CreateOrderRequest {

    @NotBlank
    private String customerId;

    @NotNull
    private OrderType orderType;

    @NotNull
    private DeliveryType deliveryType;

    @Valid
    @NotNull
    private DeliveryAddressRequest deliveryAddress;

    @Valid
    @NotEmpty
    private List<OrderItemRequest> items;

    @NotBlank
    private String paymentMode;

    private String couponCode;

    public CreateOrderRequest() {}

    public String getCustomerId()                        { return customerId; }
    public void setCustomerId(String v)                  { this.customerId = v; }

    public OrderType getOrderType()                      { return orderType; }
    public void setOrderType(OrderType v)                { this.orderType = v; }

    public DeliveryType getDeliveryType()                { return deliveryType; }
    public void setDeliveryType(DeliveryType v)          { this.deliveryType = v; }

    public DeliveryAddressRequest getDeliveryAddress()   { return deliveryAddress; }
    public void setDeliveryAddress(DeliveryAddressRequest v) { this.deliveryAddress = v; }

    public List<OrderItemRequest> getItems()             { return items; }
    public void setItems(List<OrderItemRequest> v)       { this.items = v; }

    public String getPaymentMode()                       { return paymentMode; }
    public void setPaymentMode(String v)                 { this.paymentMode = v; }

    public String getCouponCode()                        { return couponCode; }
    public void setCouponCode(String v)                  { this.couponCode = v; }
}
