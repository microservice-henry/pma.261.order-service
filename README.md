# order-service

Spring Boot service for authenticated user orders.

## Endpoints

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/orders` | Create an order for the current account. |
| `GET` | `/orders` | List order summaries for the current account. |
| `GET` | `/orders/{id}` | Get order details. Optional `currency` converts totals. |

All monetary values are stored in USD.
