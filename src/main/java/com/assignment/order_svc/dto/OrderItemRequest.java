package com.assignment.order_svc.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for a single order line item.
 */
public class OrderItemRequest {

    @NotBlank
    private String productId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    public OrderItemRequest() {}

    public String getProductId()             { return productId; }
    public void setProductId(String v)       { this.productId = v; }

    public Integer getQuantity()             { return quantity; }
    public void setQuantity(Integer v)       { this.quantity = v; }

    public BigDecimal getPrice()             { return price; }
    public void setPrice(BigDecimal v)       { this.price = v; }
}
