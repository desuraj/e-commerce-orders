package com.assignment.order_svc.service.impl;

import com.assignment.order_svc.dto.CreateOrderRequest;
import com.assignment.order_svc.dto.DeliveryAddressRequest;
import com.assignment.order_svc.dto.OrderItemRequest;
import com.assignment.order_svc.dto.OrderResponse;
import com.assignment.order_svc.enums.DeliveryType;
import com.assignment.order_svc.enums.OrderType;
import com.assignment.order_svc.strategy.OrderStrategy;
import com.assignment.order_svc.strategy.OrderStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OrderServiceImpl}.
 * Verifies that createOrder delegates to the correct strategy via OrderStrategyFactory
 * without any order-type-specific logic in the service itself.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderStrategyFactory orderStrategyFactory;

    @Mock
    private OrderStrategy mockStrategy;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CreateOrderRequest standardRequest;
    private CreateOrderRequest rushRequest;

    @BeforeEach
    void setUp() {
        DeliveryAddressRequest address = new DeliveryAddressRequest();
        address.setName("Suraj Deo");
        address.setPhone("9876543210");
        address.setAddressLine1("123 MG Road");
        address.setCity("Hyderabad");
        address.setState("Telangana");
        address.setPincode("500081");

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId("P1001");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("60.00"));

        standardRequest = new CreateOrderRequest();
        standardRequest.setCustomerId("CUST-001");
        standardRequest.setOrderType(OrderType.STANDARD);
        standardRequest.setDeliveryType(DeliveryType.STANDARD);
        standardRequest.setDeliveryAddress(address);
        standardRequest.setItems(List.of(item));
        standardRequest.setPaymentMode("ONLINE");

        rushRequest = new CreateOrderRequest();
        rushRequest.setCustomerId("CUST-002");
        rushRequest.setOrderType(OrderType.RUSH);
        rushRequest.setDeliveryType(DeliveryType.RUSH);
        rushRequest.setDeliveryAddress(address);
        rushRequest.setItems(List.of(item));
        rushRequest.setPaymentMode("ONLINE");
    }

    @Test
    @DisplayName("createOrder — STANDARD: should delegate to strategy from factory")
    void createOrder_Standard_DelegatesToStrategy() {
        when(orderStrategyFactory.getStrategy(OrderType.STANDARD)).thenReturn(mockStrategy);
        when(mockStrategy.createOrder(standardRequest)).thenReturn(new OrderResponse());

        OrderResponse result = orderService.createOrder(standardRequest);

        assertNotNull(result);
        verify(orderStrategyFactory).getStrategy(OrderType.STANDARD);
        verify(mockStrategy).createOrder(standardRequest);
    }

    @Test
    @DisplayName("createOrder — RUSH: should delegate to strategy from factory")
    void createOrder_Rush_DelegatesToStrategy() {
        when(orderStrategyFactory.getStrategy(OrderType.RUSH)).thenReturn(mockStrategy);
        when(mockStrategy.createOrder(rushRequest)).thenReturn(new OrderResponse());

        OrderResponse result = orderService.createOrder(rushRequest);

        assertNotNull(result);
        verify(orderStrategyFactory).getStrategy(OrderType.RUSH);
        verify(mockStrategy).createOrder(rushRequest);
    }
}
