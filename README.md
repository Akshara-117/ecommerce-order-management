# E-Commerce Order Management System

A Java 17 and Spring Boot REST API for managing products, customers, shopping carts, orders, and inventory with MySQL.

> **Project status:** Working local portfolio backend. The purchase flow was manually verified against a running MySQL instance on October 27, 2026, and 10 JUnit/Mockito unit tests passed locally. This is not a production-ready or publicly deployed service.

## Tech stack

- **Backend:** Java 17, Spring Boot 3.3.5, Spring Web
- **Persistence:** Spring Data JPA / Hibernate, MySQL 8
- **Validation:** Jakarta Bean Validation
- **Testing:** JUnit 5, Mockito, Maven
- **Optional local database:** Docker Compose with MySQL 8.4

## Features

- Create, list, retrieve, update, and delete products
- Register customers with validated name and email
- Add products to a customer's cart with quantity and stock checks
- Check out a cart, calculate order totals, and reduce inventory
- Clear cart items after successful checkout
- Retrieve customer order history with SQL-level customer filtering and an index on `(customer_id, created_at DESC)`
- Transactional checkout with `@Transactional`; product entity versioning for optimistic-locking support

## Architecture

```text
HTTP Client (PowerShell / Postman / curl)
               |
               v
     StoreController (/api)
               |
               v
    OrderService (business rules)
               |
               v
   Spring Data JPA Repositories
               |
               v
          MySQL Database
```

Product and customer endpoints also use repositories directly from the controller. Entity objects are currently returned directly in API responses.

## Requirements

- JDK 17+
- Maven 3.9+
- MySQL server **or** Docker Compose

## Run locally

### Option A: Existing MySQL server (Windows PowerShell)

Create an empty database with your MySQL client:

```sql
CREATE DATABASE IF NOT EXISTS ecommerce_db;
```

In PowerShell, from the folder containing `pom.xml`, set connection details **for the current terminal session**:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/ecommerce_db"
$env:DB_USER = "root"
$env:DB_PASSWORD = Read-Host "Enter MySQL password"
mvn spring-boot:run
```

Use your own database username where appropriate. Do **not** commit passwords or local environment files to GitHub.

### Option B: Docker Compose

If Docker Desktop is installed and port 3306 is free:

```bash
docker compose up -d
mvn spring-boot:run
```

The Compose configuration provides a local development MySQL database named `ecommerce` with matching default credentials in `application.properties`. These are **development-only example credentials** and must not be used for a public deployment.

The API listens on **http://localhost:8080**. The root URL `/` is not mapped; use `/api/products` to check the API.

## API reference

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/products` | List products |
| POST | `/api/products` | Create product |
| GET | `/api/products/{id}` | Retrieve product |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |
| POST | `/api/customers` | Register customer |
| GET | `/api/customers/{id}/cart` | View cart |
| POST | `/api/customers/{id}/cart` | Add cart item |
| POST | `/api/customers/{id}/checkout` | Place order |
| GET | `/api/customers/{id}/orders` | View order history |

### Example: End-to-end purchase flow (PowerShell)

Run these commands while the server is running. The IDs below assume a fresh database; use IDs returned by your API otherwise.

**1. Create a product**

```powershell
$product = @{ name = "Laptop"; price = 999.99; stock = 10 } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -ContentType "application/json" -Body $product
```

**2. Register a customer**

```powershell
$customer = @{ name = "Test Customer"; email = "testcustomer@example.com" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/customers" -Method Post -ContentType "application/json" -Body $customer
```

**3. Add two units to the cart**

```powershell
$cart = @{ productId = 1; quantity = 2 } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/customers/1/cart" -Method Post -ContentType "application/json" -Body $cart
```

**4. Check out and verify inventory**

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/customers/1/checkout" -Method Post
Invoke-RestMethod -Uri "http://localhost:8080/api/products/1" -Method Get
Invoke-RestMethod -Uri "http://localhost:8080/api/customers/1/orders" -Method Get
```

In the locally verified run, checkout created **order #1** with total **1999.98**, and the product stock changed from **10 to 8**. The order response's `items: 1` represents one order line containing two units.

## Automated tests

Run:

```bash
mvn clean test
```

The `OrderServiceTest` suite uses Mockito to isolate service logic from the database. Its 10 tests cover:

- Rejecting empty-cart checkout
- Rejecting cart additions exceeding available stock
- Rejecting zero and negative quantities
- Rejecting missing customers and products during cart additions
- Successfully adding an item to a cart
- Rejecting checkout with insufficient inventory
- Successfully creating an order, calculating the total, updating stock, and clearing the cart
- Rejecting checkout for a missing customer

**Locally observed result (October 27, 2026):** `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`.

These are unit tests, not database-backed integration tests or concurrency tests.

## Order-history query optimization

The customer order-history endpoint now filters by customer in the repository instead of loading every order and filtering in Java. The `CustomerOrder` entity also declares a composite index on `(customer_id, created_at DESC)` to support filtering and descending date order. Verify index creation in MySQL after application startup; schema updates depend on the configured Hibernate DDL behavior.

**Isolated local benchmark (October 27, 2026):** In a separate `ecommerce_benchmark` database containing 100 customers and 10,000 orders, MySQL `EXPLAIN ANALYZE` reported:

| Query / index strategy | Observed execution time |
| --- | --- |
| Full table scan, all 10,000 orders | 4.57 ms (single run) |
| Customer-ID index, 100 matching orders | 0.261 ms (single run) |
| Customer-ID index plus sort, three-run average | 0.330 ms |
| Composite customer/date index, three-run average | 0.144 ms |

The composite index removed the explicit sorting step and reduced **observed query execution time by approximately 56.5%** compared with the single-column index in the three-run sample. This is a small local SQL benchmark, **not** an API latency or production performance claim. The benchmark database is separate from `ecommerce_db`.

## Known limitations and future improvements

- No authentication or authorization; do not expose the API publicly
- Add Flyway migrations instead of relying on `ddl-auto=update`
- Add integration tests with a real database and concurrency tests for simultaneous checkout
- Improve error response consistency and HTTP status handling
- Introduce request/response DTOs instead of exposing persistence entities directly
- Add pagination, cart item update/removal, and OpenAPI documentation
- Add pagination and database-backed integration tests for order-history queries

**Security note:** This is a learning/portfolio project. Use only test customer data and local development credentials.
