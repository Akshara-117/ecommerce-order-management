# E-Commerce Order Management System

A learning-focused **Java 17 / Spring Boot / MySQL** REST backend for products, customers, shopping carts, checkout and inventory updates. Original implementation prepared as a portfolio starter; extend and test it before claiming production readiness.

## Requirements
- JDK 17+, Maven 3.9+, Docker Compose (or a running MySQL database)

## Run locally
```bash
docker compose up -d
mvn spring-boot:run
```
The API starts at `http://localhost:8080`. MySQL settings can be overridden with `DB_URL`, `DB_USER`, `DB_PASSWORD`.

## Try it
```bash
curl -X POST http://localhost:8080/api/products -H 'Content-Type: application/json' -d '{"name":"Keyboard","price":49.99,"stock":10}'
curl -X POST http://localhost:8080/api/customers -H 'Content-Type: application/json' -d '{"name":"Demo","email":"demo@example.com"}'
curl -X POST http://localhost:8080/api/customers/1/cart -H 'Content-Type: application/json' -d '{"productId":1,"quantity":2}'
curl -X POST http://localhost:8080/api/customers/1/checkout
```

## Endpoints
| Method | Path | Description |
|---|---|---|
| GET / POST | `/api/products` | List / create products |
| GET / PUT / DELETE | `/api/products/{id}` | Read / update / delete a product |
| POST | `/api/customers` | Create customer |
| GET / POST | `/api/customers/{id}/cart` | View / add cart items |
| POST | `/api/customers/{id}/checkout` | Place order, deduct stock, clear cart |
| GET | `/api/customers/{id}/orders` | Basic order history |

## Architecture
Controller → Service → Spring Data JPA repositories → MySQL. Checkout is transactional, prices are stored as `BigDecimal`, and inventory uses JPA optimistic locking (`@Version`) to detect concurrent writes.

## Tests
```bash
mvn test
```
Includes unit tests for empty carts and insufficient inventory. **Not yet run in this delivery environment.**

## Next improvements
- Add Spring Security authentication/authorization and protect all write endpoints
- Replace JPA `ddl-auto=update` with Flyway migrations
- Add pagination, cart item update/remove, proper 404 responses, API DTOs, and OpenAPI docs
- Add integration tests with Testcontainers, concurrency tests, CI, and production-safe configuration
- Avoid exposing persistence entities directly from controllers

**Important:** This is a portfolio starter, not a deployed production system. No authentication is implemented; do not expose it publicly or use real customer data.
