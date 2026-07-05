# E-Commerce Rush Delivery — Feature Detail

---

## Table of Contents

1. [Overview](#1-overview)
2. [Functional Requirements](#2-functional-requirements)
3. [Non-Functional Requirements](#3-non-functional-requirements)
4. [Create Order Flow](#4-create-order-flow)
5. [REST API Design](#5-rest-api-design)
6. [Validation Design](#6-validation-design)
7. [Kafka Event Design](#7-kafka-event-design)
8. [Real-Time Order Status Updates](#8-real-time-order-status-updates)
9. [High Traffic Handling](#9-high-traffic-handling)
10. [Failure Handling](#10-failure-handling)
11. [Design Decisions](#11-design-decisions)

> **Note:** High-Level Architecture, Service Responsibilities, and Database Design are documented in `e-commerce-HLD.md`.

---

## 1. Overview

This document consolidates the full design for a **Spring Boot based E-Commerce Order Service** supporting **Rush Delivery (2-hour SLA)** alongside standard delivery, while handling high-volume traffic with reliability and scalability.

Rush Delivery is a quick-commerce feature that allows customers to receive their orders within a guaranteed short window, subject to order value and delivery zone eligibility.

---

## 2. Functional Requirements

1. Customers can place a **normal order**.
2. Customers can place an order with the **Rush Delivery** option.
3. Rush Delivery is available only when:
   - Order value is **greater than or equal to $100**.
   - Delivery address belongs to a configured **Rush Service Zone**.
4. If the minimum order value is not met, the system must reject the Rush Delivery request with a clear error message.
5. If the delivery address is outside the Rush Service Zone, the system must reject the Rush Delivery request and may offer standard delivery as an alternative.
6. The application must provide **real-time order status updates** to customers, including any changes to the estimated delivery window.
7. The system must handle **high traffic** without performance degradation.

---

## 3. Non-Functional Requirements

| Attribute | Description |
|-----------|-------------|
| **High Availability** | Service must remain reachable even during partial failures |
| **High Throughput** | Support a large number of concurrent order requests |
| **Low Latency** | Order creation and validation must respond quickly |
| **Horizontal Scalability** | Services must scale out on demand |
| **Fault Tolerance** | Failures in one service must not cascade |
| **Event-Driven Architecture** | Asynchronous communication using Kafka |
| **Secure** | API Gateway handles auth, TLS enforced |
| **Observable** | Distributed tracing, structured logging, and metrics |

---

## 4. Create Order Flow

The flow follows the **Strategy Pattern** — `OrderService` delegates to the correct strategy via `OrderStrategyFactory` based on the `orderType` field (`STANDARD` or `RUSH`) in the request.

```text
          Client
            │
            ▼
  [ POST /api/v1/orders ]
            │
            ▼
      [ API Gateway ]
            │
            ▼
    [ OrderController ]
            │
            ▼
      [ OrderService ]
            │
            ▼
          ◇ OrderStrategyFactory ◇
               orderType = ?
              /               \
        "RUSH"               "STANDARD"
            │                     │
            ▼                     ▼
  ┌──────────────────┐   ┌─────────────────────┐
  │ RushOrderStrategy│   │ StandardOrderStrategy│
  └────────┬─────────┘   └──────────┬──────────┘
           │                        │
           ▼                        │
   [ Calculate Total ]              │
           │                        │
           ▼                        │
   ◇ Amount >= $100? ◇              │
   │ No → 400 Bad Request           │
   │ "Min $100 for Rush Delivery"   │
   └── Yes                          │
           │                        │
           ▼                        │
   ◇ Rush Zone Valid? ◇             │
   │ No → 400 Bad Request           │
   │ "Rush unavailable for zone"    │
   └── Yes                          │
           │                        │
           └──────────┬─────────────┘
                       │
                       ▼
             [ InventoryService ]
              ├─ Out of Stock → 400 Error
              └─ Stock Reserved
                       │
                       ▼
              [ PaymentService ]
              ├─ Failure → Release Inventory → Error
              └─ Success
                       │
                       ▼
             [ OrderRepository ]
          (Persist Order to PostgreSQL)
                       │
                       ▼
        [ Publish Kafka Event : OrderCreated ]
                       │
                       ▼
               [ Kafka Broker ]
                       │
                       ▼
          [ Notification Service ]
                       │
                       ▼
                 [ WebSocket ]
                       │
                       ▼
              [ Customer App ]
```

---

## 5. REST API Design

### Create Order

```http
POST /api/v1/orders
Content-Type: application/json
```

**Request Body — Standard Order:**

```json
{
  "customerId": "CUST12345",
  "orderType": "STANDARD",
  "deliveryType": "STANDARD",
  "deliveryAddress": {
    "name": "Suraj Deo",
    "phone": "9876543210",
    "addressLine1": "123 MG Road",
    "addressLine2": "Near Metro Station",
    "city": "Hyderabad",
    "state": "Telangana",
    "pincode": "500081"
  },
  "items": [
    {
      "productId": "P1001",
      "quantity": 2
    },
    {
      "productId": "P2005",
      "quantity": 1
    }
  ],
  "paymentMode": "ONLINE",
  "couponCode": "SAVE100"
}
```

**Request Body — Rush Order:**

```json
{
  "customerId": "CUST12345",
  "orderType": "RUSH",
  "deliveryType": "RUSH",
  "deliveryAddress": {
    "name": "Suraj Deo",
    "phone": "9876543210",
    "addressLine1": "123 MG Road",
    "addressLine2": "Near Metro Station",
    "city": "Hyderabad",
    "state": "Telangana",
    "pincode": "500081"
  },
  "items": [
    {
      "productId": "P1001",
      "quantity": 2
    },
    {
      "productId": "P2005",
      "quantity": 1
    }
  ],
  "paymentMode": "ONLINE",
  "couponCode": "SAVE100"
}
```

**Success Response: `201 Created` — Standard Order**

```json
{
  "orderId": "ORD12345",
  "status": "CONFIRMED",
  "orderType": "STANDARD",
  "deliveryType": "STANDARD",
  "estimatedDelivery": "2:00 PM",
  "totalAmount": 155.00
}
```

**Success Response: `201 Created` — Rush Order**

```json
{
  "orderId": "ORD12346",
  "status": "CONFIRMED",
  "orderType": "RUSH",
  "deliveryType": "RUSH",
  "estimatedDelivery": "2:00 PM",
  "totalAmount": 155.00
}
```

**Error Responses:**

| HTTP Code | Scenario | Message |
|-----------|----------|---------|
| 400 | Order total below $100 for orderType RUSH | "Minimum order value for Rush Delivery is $100." |
| 400 | Delivery address outside rush zone | "Rush Delivery unavailable for this location." |
| 400 | Out of stock | "Product unavailable." |
| 402 | Payment failure | "Payment could not be processed." |
| 422 | Validation error | Field-specific validation messages |

---

### Get Order by ID

```http
GET /api/v1/orders/{id}
```

**Response: `200 OK`**

```json
{
  "orderId": "ORD12345",
  "status": "OUT_FOR_DELIVERY",
  "orderType": "RUSH",
  "deliveryType": "RUSH",
  "estimatedDelivery": "2:30 PM"
}
```

---

### Delivery Eligibility Check (internal service call)

```http
POST /delivery/check
Content-Type: application/json
```

**Request:**

```json
{
  "pincode": "500032"
}
```

**Response — Eligible:**

```json
{
  "eligible": true
}
```

**Response — Not Eligible:**

```json
{
  "eligible": false,
  "reason": "Rush delivery unavailable for this location."
}
```

---

### WebSocket — Real-Time Order Updates

```http
GET /ws/orders
```

The customer subscribes by orderId and receives push updates without polling.

---

### API Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Create Order (normal or rush) |
| GET | `/api/v1/orders/{id}` | Get Order Details |
| POST | `/delivery/check` | Validate Rush Delivery Eligibility |
| GET | `/ws/orders` | WebSocket — Live Order Status Updates |

---

## 6. Validation Design

### Price Threshold Validation — Order Service

```java
BigDecimal total = calculateOrderTotal(items);

if ("RUSH".equals(request.getOrderType()) && total.compareTo(new BigDecimal("100")) < 0) {
    throw new BusinessException(
        "Minimum order value for Rush Delivery is $100."
    );
}
```

> Threshold is externalized to `application.properties`:
> ```properties
> order.rush.minimum-total=100.00
> ```

---

### Rush Zone Validation — Delivery Service

```java
boolean eligible = rushZoneRepository.existsByPincodeAndIsActiveTrue(pincode);
```

- Queries `rush_zone_master` by pincode
- Zone data is managed by business users without code changes
- Leverages Redis cache for fast repeated lookups

---

### Validation Boundaries

| Concern | Owner |
|---------|-------|
| Request structure (format, required fields) | Controller (Bean Validation) |
| Minimum order value for rush | Order Service |
| Rush zone eligibility | Delivery Service |
| Stock availability | Inventory Service |
| Payment processing | Payment Service |

---

## 7. Kafka Event Design

### Events Published

| Event | Published By | Consumed By |
|-------|-------------|-------------|
| `OrderCreated` | Order Service | Notification Service |
| `InventoryReserved` | Inventory Service | Order Service |
| `PaymentCompleted` | Payment Service | Order Service |
| `OrderConfirmed` | Order Service | Notification Service |
| `OrderCancelled` | Order Service | Notification, Inventory Service |
| `OrderStatusUpdated` | Order / Delivery Service | Notification Service |

### Sample Event Payload

```json
{
  "orderId": "ORD12345",
  "status": "OUT_FOR_DELIVERY",
  "estimatedDeliveryTime": "2:30 PM",
  "message": "Delivery delayed due to traffic."
}
```

---

## 9. Real-Time Order Status Updates

### Requirement

Customers must receive real-time updates whenever order status or estimated delivery time changes, without the need to poll the API.

### Design

The Order Service and Delivery Service publish Kafka events whenever order state or ETA changes. The Notification Service consumes these events and pushes updates to connected clients via WebSocket.

### Flow

```text
Order Service / Delivery Service
            │
Order Status Changed or ETA Updated
            │
            ▼
Publish Kafka Event (OrderStatusUpdated)
            │
            ▼
Notification Service consumes event
            │
            ▼
WebSocket push to subscribed client
            │
            ▼
Customer App updates instantly — no polling needed
```

### Notification Service Responsibilities

- Consume order lifecycle events from Kafka
- Push updates to customers via WebSocket
- Notify customers when:
  - Order is confirmed
  - Order is out for delivery
  - Estimated delivery window changes
  - Order is delivered
  - Order is cancelled

### Benefits

| Benefit | Description |
|---------|-------------|
| No client polling | Server pushes updates proactively |
| Reduced server load | Fewer GET /orders calls from clients |
| Near real-time experience | Updates arrive immediately on status changes |
| Loose coupling | Notification Service is decoupled from Order Service via Kafka |
| Scalable | Notification Service can scale independently |

---

## 10. High Traffic Handling

| Strategy | Description |
|----------|-------------|
| **Load Balancer** | Distributes traffic across multiple service instances |
| **Horizontal Scaling** | Multiple Order Service instances behind the load balancer |
| **Kubernetes Auto Scaling** | HPA scales pods based on CPU and request volume |
| **Redis Cache** | Rush zone lookups and frequently accessed data served from cache |
| **HikariCP Connection Pool** | Manages database connections efficiently |
| **Kafka** | Asynchronous communication decouples Order Service from downstream services |
| **API Gateway** | Rate limiting and request throttling at the entry point |

---

## 11. Failure Handling

| Scenario | Action |
|----------|--------|
| Order total below $100 with orderType RUSH | Return `400 Bad Request` — minimum value error |
| Delivery address outside rush zone | Return `400 Bad Request` — zone ineligibility error |
| Out of stock | Return error — order not created |
| Payment failure | Release reserved inventory — return payment failure error |
| Delivery Service unavailable | Reject rush order gracefully — fallback to standard delivery option |
| Kafka publish failure | Retry or dead-letter queue — order persisted, notification delayed |

---

## 11. Design Decisions

| Decision | Reason |
|----------|--------|
| **Microservices Architecture** | Independent scaling of Order, Delivery, Inventory, Payment, and Notification services |
| **Dedicated Delivery Service** | Encapsulates delivery eligibility logic and supports future rule additions without changing the order workflow |
| **Rush Zone Master Table** | Business-configurable zones managed without code changes |
| **Redis** | Fast in-memory lookup for rush zone eligibility and caching |
| **Kafka** | Loose coupling, reliable event delivery, and asynchronous communication between services |
| **PostgreSQL** | ACID compliance for order persistence |
| **WebSocket** | Real-time push updates without client polling |
| **API Gateway** | Centralised routing, authentication, rate limiting, and TLS termination |
| **Kubernetes** | Horizontal auto-scaling based on load |
| **HikariCP** | Efficient database connection pool management |

---
