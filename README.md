# E-Commerce Order Management System

A Java 17 / Spring Boot 3.3.5 REST API demonstrating e-commerce workflows, JWT authentication, relational persistence, automated tests, CI, and Docker packaging.

**Status:** Local portfolio project, not a production deployment. The Docker image, MySQL connection, products API, and Swagger UI were manually verified on October 27, 2026.

## Tech stack

- Java 17, Spring Boot 3.3.5, Maven
- Spring Web, Spring Data JPA / Hibernate, Jakarta Validation
- Spring Security, OAuth2 Resource Server / JOSE, HS256 JWT, BCrypt
- MySQL 8; H2 for isolated integration tests
- JUnit 5, Mockito, MockMvc
- Springdoc OpenAPI / Swagger UI
- Docker multi-stage build; GitHub Actions CI

## Features

- Product CRUD, customer registration, cart management, transactional checkout, inventory deduction, and order history
- Customer email/password registration and login; BCrypt password hashes stored in a separate credential table
- One-hour signed JWT access tokens and customer ownership authorization
- Customer-specific order history, newest first, with optional pagination and bounded page sizes
- Repository-level filtering and composite index on `(customer_id, created_at DESC)`
- Consistent 400/404 API error handling; stock validation and optimistic-locking support
- OpenAPI documentation and a non-root Docker runtime image

## Architecture

```text
HTTP / Swagger
     |
Spring Security (JWT validation + authorization)
     |
REST Controllers -> Services (business logic, transactions)
     |
Spring Data JPA Repositories
     |
MySQL
```

## Local setup

Requirements: JDK 17+, Maven 3.9+, and MySQL 8. Create a development database, for example `ecommerce_db`.

In Windows PowerShell, from the directory containing `pom.xml`:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/ecommerce_db"
$env:DB_USER = "root"
$env:DB_PASSWORD = Read-Host "MySQL password"
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
mvn spring-boot:run
```

The API is available at `http://localhost:8080`; Swagger UI is at `http://localhost:8080/swagger-ui/index.html`. Secrets are environment variables, not committed configuration. Generating a new JWT secret invalidates tokens issued with a previous secret.

### Docker image (using an existing MySQL server on Windows)

Build:

```powershell
docker build -t ecommerce-order-management:1.0 .
```

With `DB_PASSWORD` and `JWT_SECRET` set in the **same PowerShell session**, run:

```powershell
docker run --name ecommerce-app `
  -p 8081:8080 `
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/ecommerce_db" `
  -e DB_USER="root" `
  -e DB_PASSWORD="$env:DB_PASSWORD" `
  -e JWT_SECRET="$env:JWT_SECRET" `
  ecommerce-order-management:1.0
```

The Dockerized API is at `http://localhost:8081/api/products` and Swagger UI is at `http://localhost:8081/swagger-ui/index.html`. Port 8081 avoids conflicts with a locally running Spring Boot server on port 8080. `host.docker.internal` refers to the Windows host from inside Docker Desktop.

**Data safety:** This configuration connects to your existing MySQL database. Hibernate currently uses `ddl-auto=update`, which can change schema on startup. Back up important data before running against an existing database. The repository's `docker-compose.yml` is a separate, development-only MySQL service; do not start it when host port 3306 is already occupied. Do not commit passwords or JWT secrets.

## API overview

| Method | Path | Access |
| --- | --- | --- |
| POST | `/api/auth/register` | Public; create customer credentials |
| POST | `/api/auth/login` | Public; obtain bearer JWT |
| GET | `/api/products` | Public |
| GET | `/api/products/{id}` | Public |
| GET | `/api/customers/{id}/cart` | JWT for matching customer |
| POST | `/api/customers/{id}/cart` | JWT for matching customer |
| POST | `/api/customers/{id}/checkout` | JWT for matching customer |
| GET | `/api/customers/{id}/orders` | JWT for matching customer |
| GET | `/api/customers/{id}/orders/paged?page=0&size=10` | JWT for matching customer |

Product mutation routes and the legacy customer-creation route may exist in controllers, but **are intentionally denied by the current security configuration** until administrative roles are implemented. Other unmatched routes are denied by default.

Register/login requests use JSON; login accepts `email` and `password`. Use the returned `accessToken` in `Authorization: Bearer <token>` for protected endpoints. JWT subject must match the customer ID in the path. Do not paste real tokens into issues or public documentation.

## Tests and CI

```bash
mvn clean test
```

**Verified locally on October 27, 2026:** 25 automated tests passed, zero failures/errors, including service unit tests, controller tests, H2 repository integration tests, exception handling, authentication service tests, and security integration tests.

GitHub Actions runs Maven clean/test on pushes and pull requests. A successful CI run was confirmed on October 27, 2026.

## Order-history optimization

Order history is filtered in SQL rather than loading all orders into Java. The entity declares an index on `(customer_id, created_at DESC)`; paged results sort by creation date and ID descending.

In an **isolated** 10,000-order MySQL benchmark (October 27, 2026), the observed three-run average was 0.330 ms using a customer-ID index plus sorting versus 0.144 ms with the composite index: about **56.5% lower SQL execution time** in this small sample. This is **not** a production or end-to-end API latency claim.

## Current limitations

- Local-only validation; not hardened or deployed publicly
- No admin role yet; product writes are currently blocked by authorization
- Replace Hibernate `ddl-auto=update` with versioned migrations (e.g., Flyway)
- Add concurrency and real-MySQL integration tests, and expand DTO usage
- Use dedicated application DB credentials, managed secrets, and production deployment controls before hosting publicly

Use test data only. This repository is an interview/learning portfolio project.
