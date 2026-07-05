# Create Order — Front-End ↔ Back-End Activity Diagram

---

## Swim Lanes

```
┌─────────────────────────────────────────┬──────────────────────────────────────────────────────────────────┐
│              FRONT-END                  │                         BACK-END                                  │
│           (Customer App)                │          API Gateway → Controller → Service → Strategy            │
└─────────────────────────────────────────┴──────────────────────────────────────────────────────────────────┘
```

---

## Activity Diagram

```text
         FRONT-END                                          BACK-END
       (Customer App)                        API Gateway → Controller → Service → Strategy
─────────────────────────────────────────────────────────────────────────────────────────────

  ●  START
       │
       ▼
 ┌───────────────────────┐
 │  User fills the form  │
 │  ─────────────────    │
 │  • customerId         │
 │  • orderType          │
 │    STANDARD or RUSH   │
 │  • deliveryType       │
 │  • deliveryAddress    │
 │    (name, phone,      │
 │     address, pincode) │
 │  • items              │
 │    (productId, qty,   │
 │     price)            │
 │  • paymentMode        │
 │  • couponCode         │
 └───────────┬───────────┘
             │
             │  ──── POST /api/orders ──────────────────────────────────────►
             │       Content-Type: application/json
             │       {
             │         "customerId": "CUST12345",
             │         "orderType": "STANDARD" | "RUSH",
             │         "deliveryType": "STANDARD" | "RUSH",
             │         "deliveryAddress": { name, phone,
             │                              addressLine1, addressLine2,
             │                              city, state, pincode },
             │         "items": [{ productId, quantity, price }],
             │         "paymentMode": "ONLINE",
             │         "couponCode": "SAVE100"
             │       }
             │                                                                │
             │                                                       ┌────────┴────────┐
             │                                                       │   API Gateway   │
             │                                                       │  (auth+routing) │
             │                                                       └────────┬────────┘
             │                                                                │
             │                                                       ┌────────┴────────┐
             │                                                       │ OrderController  │
             │                                                       │ @Valid on body   │
             │                                                       └────────┬────────┘
             │                                                                │
             │                                                       ◇ Fields valid?
             │  ◄──── 400 (field errors) ─────────────────────────── No
             │        Show validation errors on form
             │                                                        └── Yes
             │                                                                │
             │                                                       ┌────────┴────────┐
             │                                                       │  OrderService   │
             │                                                       │ (orchestrator)  │
             │                                                       └────────┬────────┘
             │                                                                │
             │                                                       ◇ OrderStrategyFactory
             │                                                         orderType = ?
             │                                                        /               \
             │                                                     RUSH            STANDARD
             │                                                      │                  │
             │                                                      ▼                  │
             │                                             ┌─────────────────┐         │
             │                                             │ RushOrderStrategy│         │
             │                                             └────────┬────────┘         │
             │                                                      │                  │
             │                                             Calculate Total              │
             │                                                      │                  │
             │                                             ◇ Total >= $100?             │
             │  ◄──── 400 "Min $100 for Rush Delivery" ──── No                         │
             │        Show error on form                    └── Yes                    │
             │                                                      │                  │
             │                                             ◇ Rush Zone Valid?           │
             │  ◄──── 400 "Rush unavailable for zone" ──── No                          │
             │        Show error on form                    └── Yes                    │
             │                                                      │                  │
             │                                                      └──────────┬───────┘
             │                                                                 │
             │                                                        ┌────────┴────────┐
             │                                                        │ InventoryService │
             │                                                        │ (reserve stock)  │
             │                                                        └────────┬─────────┘
             │                                                                 │
             │                                                        ◇ Stock Available?
             │  ◄──── 400 "Product unavailable" ───────────────────── No
             │        Show out-of-stock message                        └── Yes — Reserved
             │                                                                 │
             │                                                        ┌────────┴────────┐
             │                                                        │  PaymentService  │
             │                                                        │(process payment) │
             │                                                        └────────┬─────────┘
             │                                                                 │
             │                                                        ◇ Payment Success?
             │  ◄──── 402 "Payment could not be processed" ─────────── No → Release Inventory
             │        Show payment failure message                     └── Yes
             │                                                                 │
             │                                                        ┌────────┴────────┐
             │                                                        │ OrderRepository  │
             │                                                        │(persist to DB)   │
             │                                                        └────────┬─────────┘
             │                                                                 │
             │                                                        Publish Kafka Event
             │                                                        (OrderCreated)
             │                                                                 │
             │  ◄──── 201 Created ─────────────────────────────────────────── │
             │        {                                                         │
             │          "orderId":          "ORD12345",                        │
             │          "status":           "CONFIRMED",                       │
             │          "orderType":        "STANDARD" | "RUSH",              │
             │          "deliveryType":     "STANDARD" | "RUSH",              │
             │          "totalAmount":      155.00,                            │
             │          "estimatedDelivery": "2:00 PM"                        │
             │        }                                                         │
             │
  ┌──────────┴──────────────┐
  │  Show order confirmation │
  │  screen to user          │
  │  ─────────────────────  │
  │  • orderId              │
  │  • status: CONFIRMED    │
  │  • totalAmount          │
  │  • estimatedDelivery    │
  └─────────────────────────┘
             │
  ◉  END  (HTTP flow complete)


─────────────────────────────────────────────────────────────────────────────────────────────
 POST-RESPONSE: Real-Time Updates via WebSocket (async, after 201)
─────────────────────────────────────────────────────────────────────────────────────────────

  FRONT-END                                  BACK-END (Kafka + Notification Service)
─────────────────────────────────────────────────────────────────────────────────────────────

  (connected to WS /ws/orders)
             │
             │  ◄══ WS push: status = "OUT_FOR_DELIVERY" ═══════════════════
             │      Update order status on screen
             │
             │  ◄══ WS push: status = "DELIVERED" ══════════════════════════
             │      Show delivered confirmation
             │
  ◉  END  (order lifecycle complete)
```

---

## Error Response Summary

| HTTP Code | When                           | Message                                        | FE Action              |
|-----------|--------------------------------|------------------------------------------------|------------------------|
| 400       | Invalid / missing fields       | Field-level validation messages                | Highlight form fields  |
| 400       | RUSH total < $100              | "Minimum order value for Rush Delivery is $100." | Show error banner    |
| 400       | RUSH zone ineligible           | "Rush Delivery unavailable for this location." | Show error banner      |
| 400       | Out of stock                   | "Product unavailable."                         | Show stock error       |
| 402       | Payment failed                 | "Payment could not be processed."              | Show payment error     |
| 201       | Success                        | Full OrderResponse body                        | Show confirmation      |
