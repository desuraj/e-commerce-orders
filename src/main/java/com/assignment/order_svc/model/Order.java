package com.assignment.order_svc.model;

import com.assignment.order_svc.enums.DeliveryType;
import com.assignment.order_svc.enums.OrderStatus;
import com.assignment.order_svc.enums.OrderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing an order in the e-commerce system.
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
    @Index(name = "idx_orders_status",      columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "items")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false, length = 20)
    private DeliveryType deliveryType;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating an Order.
     *
     * @param customerId   the customer identifier
     * @param orderType    STANDARD or RUSH
     * @param deliveryType STANDARD or RUSH
     */
    public Order(String customerId, OrderType orderType, DeliveryType deliveryType) {
        this.customerId   = customerId;
        this.orderType    = orderType;
        this.deliveryType = deliveryType;
        this.status       = OrderStatus.PENDING;
        this.totalAmount  = BigDecimal.ZERO;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** Adds an item and maintains the bidirectional relationship. */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /** Calculates total from all item lines (price × quantity). */
    public BigDecimal calculateTotalAmount() {
        return items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
