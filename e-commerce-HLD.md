# E-Commerce — High-Level Design

---

## High-Level Architecture

```text
                Internet
                    │
            Load Balancer
                    │
              API Gateway
                    │
             Order Service
                    │
     ┌──────────────┼──────────────┐
     │              │              │
Inventory      Delivery      Payment
 Service       Service        Service
     │              │              │
     └──────────────┼──────────────┘
                    │
                  Kafka
                    │
        Notification Service
                    │
                WebSocket
                    │
         Customer Application

         PostgreSQL + Redis
```

**Traffic path:**
- Client → Load Balancer → API Gateway → Order Service
- Order Service coordinates with Delivery, Inventory, and Payment services synchronously
- On success, Order Service publishes Kafka events consumed by Notification Service
- Notification Service pushes real-time updates to Customer Application via WebSocket

---

## Service Responsibilities

| Service | Responsibility |
|---|---|
| **Order Service** | Entry point for all order requests; orchestrates validation, stock reservation, payment, and persistence. |
| **Delivery Service** | Validates whether the customer's delivery address falls within a configured Rush Service Zone. |
| **Inventory Service** | Checks and reserves product stock on order confirmation; releases stock on payment failure or cancellation. |
| **Payment Service** | Processes customer payment and notifies Order Service of success or failure. |
| **Notification Service** | Consumes Kafka order events and pushes real-time status updates to customers via WebSocket, Email, and SMS. |

---

## Database Design

### Table: orders

```sql
CREATE TABLE orders (
    id             BIGSERIAL PRIMARY KEY,
    customer_id    BIGINT        NOT NULL,
    total_amount   DECIMAL(19,2) NOT NULL,
    rush_delivery  BOOLEAN       NOT NULL DEFAULT FALSE,
    status         VARCHAR(20)   NOT NULL,
    created_at     TIMESTAMP     NOT NULL
);
```

### Table: order_items

```sql
CREATE TABLE order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT        NOT NULL REFERENCES orders(id),
    product_id  BIGINT        NOT NULL,
    quantity    INTEGER       NOT NULL,
    price       DECIMAL(19,2) NOT NULL
);
```

### Table: products

```sql
CREATE TABLE products (
    id     BIGSERIAL PRIMARY KEY,
    name   VARCHAR(255)  NOT NULL,
    price  DECIMAL(19,2) NOT NULL,
    stock  INTEGER       NOT NULL
);
```

### Table: rush_zone_master

```sql
CREATE TABLE rush_zone_master (
    id        BIGSERIAL PRIMARY KEY,
    pincode   VARCHAR(20)  NOT NULL,
    city      VARCHAR(100) NOT NULL,
    is_active BOOLEAN      NOT NULL DEFAULT TRUE
);
```

> Business users maintain the `rush_zone_master` table. Activating or deactivating a zone requires no code change.

### Delivery Address

```sql
-- delivery_address embedded in orders or as a separate table
-- Fields: address_line1, city, pincode
```
