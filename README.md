# Order Service

Spring Boot microservice for e-commerce order management supporting **Standard** and **Rush Delivery (2-hour SLA)** orders.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot |
| Database | H2 (dev) / PostgreSQL (prod) |
| ORM | Spring Data JPA |
| Build | Maven |
| Testing | JUnit 5 + Mockito |

---

## Design Pattern — Strategy

`OrderService` delegates to the correct strategy via `OrderStrategyFactory` based on `orderType`. No if/else in the service layer.

```
POST /api/orders
      │
 OrderController
      │
 OrderService
      │
 ◇ OrderStrategyFactory
  /               \
RUSH           STANDARD
  │                │
RushOrderStrategy  StandardOrderStrategy
(total ≥ $100,     (inventory → payment
 zone check,        → save → publish)
 inventory → payment
 → save → publish)
```

---

## Project Structure

```
src/main/java/com/assignment/order_svc/
├── controller/     OrderController.java
├── service/        OrderService.java + impl/OrderServiceImpl.java
├── strategy/       OrderStrategy, OrderStrategyFactory,
│                   StandardOrderStrategy, RushOrderStrategy
├── model/          Order.java, OrderItem.java
├── enums/          OrderType, DeliveryType, OrderStatus
├── dto/            CreateOrderRequest, DeliveryAddressRequest,
│                   OrderItemRequest, OrderResponse
└── repository/     OrderRepository.java
```

---

## API

### `POST /api/orders` — Create Order

**Request Body:**
```json
{
  "customerId": "CUST12345",
  "orderType": "STANDARD",
  "deliveryType": "STANDARD",
  "deliveryAddress": {
    "name": "Suraj Deo",
    "phone": "9876543210",
    "addressLine1": "123 MG Road",
    "city": "Hyderabad",
    "state": "Telangana",
    "pincode": "500081"
  },
  "items": [{ "productId": "P1001", "quantity": 2, "price": 60.00 }],
  "paymentMode": "ONLINE",
  "couponCode": "SAVE100"
}
```

> Use `"orderType": "RUSH"` + `"deliveryType": "RUSH"` for rush delivery.
> Rush requires total ≥ $100 and a valid Rush Zone pincode.

**Responses:**

| Code | Scenario |
|------|----------|
| `201` | Order created |
| `400` | Validation error / RUSH total < $100 / zone ineligible / out of stock |
| `402` | Payment failed |

---

## Run Locally

```bash
./mvnw spring-boot:run
```

- App → `http://localhost:8080`
- H2 Console → `http://localhost:8080/h2-console` (URL: `jdbc:h2:mem:orderdb`)

---

## Run Tests

```bash
./mvnw test
```

---

## Postman Collection

Import [`api-collection/order-service.postman_collection.json`](api-collection/order-service.postman_collection.json) — set `{{baseUrl}}` to `http://localhost:8080`.

---

## Documentation

### [`e-commerce-HLD.md`](e-commerce-HLD.md) — High-Level Design

System-wide architecture reference. Covers:

| Section | What's inside |
|---|---|
| High-Level Architecture | ASCII diagram — Internet → Load Balancer → API Gateway → Order Service → Inventory / Delivery / Payment → Kafka → Notification → WebSocket → Customer |
| Service Responsibilities | 1–2 line summary of what each service owns (Order, Delivery, Inventory, Payment, Notification) |
| Database Design | SQL table definitions — `orders`, `order_items`, `products`, `rush_zone_master`, delivery address |

---

### [`E-Commerce_Rush_feature.md`](E-Commerce_Rush_feature.md) — Feature Detail

Full design specification for the Rush Delivery feature. Covers:

| Section | What's inside |
|---|---|
| Overview | What the feature is and why it exists |
| Functional Requirements | Normal order, Rush order, eligibility rules ($100 min + zone) |
| Non-Functional Requirements | High availability, throughput, latency, Kafka, security |
| Create Order Flow | Strategy Pattern flow — RUSH vs STANDARD with decision points |
| REST API Design | Request body, success responses (STANDARD & RUSH), error codes |
| Validation Design | Price threshold check, Rush Zone check, validation boundaries |
| Kafka Event Design | Events published, who produces and consumes each event |
| Real-Time Order Status | WebSocket flow — how live updates reach the customer |
| High Traffic Handling | Load balancer, Redis cache, HikariCP, Kubernetes HPA |
| Failure Handling | All failure scenarios and their actions |
| Design Decisions | Why each technology and pattern was chosen |

---

### [`api-collection/create-order-activity-diagram.md`](api-collection/create-order-activity-diagram.md) — Activity Diagram

Front-end ↔ Back-end swim-lane diagram for the Create Order API showing every request, decision, error path, and real-time WebSocket push.
