package com.assignment.order_svc.strategy;

import com.assignment.order_svc.enums.OrderType;
import org.springframework.stereotype.Component;

/**
 * Selects the correct {@link OrderStrategy} based on the incoming {@link OrderType}.
 *
 * Adding a new order type only requires registering a new strategy here —
 * OrderService never needs to change (Open/Closed Principle).
 */
@Component
public class OrderStrategyFactory {

    private final StandardOrderStrategy standardOrderStrategy;
    private final RushOrderStrategy     rushOrderStrategy;

    public OrderStrategyFactory(StandardOrderStrategy standardOrderStrategy,
                                RushOrderStrategy rushOrderStrategy) {
        this.standardOrderStrategy = standardOrderStrategy;
        this.rushOrderStrategy     = rushOrderStrategy;
    }

    /**
     * Returns the strategy for the given order type.
     *
     * @param orderType the order type from the request
     * @return the matching {@link OrderStrategy}
     * @throws IllegalArgumentException for unsupported order types
     */
    public OrderStrategy getStrategy(OrderType orderType) {
        switch (orderType) {
            case STANDARD: return standardOrderStrategy;
            case RUSH:     return rushOrderStrategy;
            default:
                throw new IllegalArgumentException("Unsupported order type: " + orderType);
        }
    }
}
