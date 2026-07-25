# Financial Portfolio Backend

A Spring Boot backend providing JWT-based authentication as the foundation for a financial portfolio application.

## Tech Stack

- Java 21
- Spring Boot 4 (Web, Data JPA, Security, Validation)
- PostgreSQL
- Flyway (schema migrations)
- [jjwt](https://github.com/jwtk/jjwt) (JWT generation/validation)
- [Bucket4j](https://bucket4j.com/) (in-memory rate limiting)
- Lombok

## Prerequisites

- Java 21
- PostgreSQL (locally or via Docker)
- The included Maven wrapper (`./mvnw`) — no local Maven install needed

## Configuration

The app is configured entirely through environment variables (see `application.yml`).

| Variable | Required | Default | Description |
|---|---|---|---|
| `DB_USERNAME` | yes | — | Postgres username |
| `DB_PASSWORD` | yes | — | Postgres password |
| `JWT_SECRET` | yes | — | Base64-encoded HMAC key, ≥ 32 bytes. Generate with `openssl rand -base64 32` |
| `DB_HOST` | no | `localhost` | Postgres host |
| `DB_PORT` | no | `5432` | Postgres port |
| `DB_NAME` | no | `financial_portfolio` | Postgres database name |
| `SERVER_PORT` | no | `8080` | Port the app listens on |
| `JWT_EXPIRATION_MS` | no | `3600000` (1 hour) | JWT expiration in milliseconds |
| `SPRING_PROFILES_ACTIVE` | no | `dev` | Active Spring profile (`dev` / `prod`) |

## Running locally

1. Start Postgres, e.g. via Docker:
   ```
   docker run -d --name financial-portfolio-db \
     -e POSTGRES_DB=financial_portfolio \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=devpass \
     -p 5432:5432 postgres:16
   ```
2. Export the required environment variables (or configure them in your IDE run configuration):
   ```
   export DB_USERNAME=postgres
   export DB_PASSWORD=devpass
   export JWT_SECRET=$(openssl rand -base64 32)
   ```
3. Run the app:
   ```
   ./mvnw spring-boot:run
   ```

Flyway applies pending migrations automatically on startup — no manual schema setup needed.

## API

### Health

```
GET /api/health
```
Public. Returns `200 { "status": "UP" }`.

### Auth

```
POST /api/auth/register
Content-Type: application/json

{ "email": "user@example.com", "password": "..." }
```
Hashes the password (BCrypt), creates the user, and returns a JWT.
- `201` → `{ "token": "<jwt>" }`
- `409` → email already registered

```
POST /api/auth/login
Content-Type: application/json

{ "email": "user@example.com", "password": "..." }
```
Validates credentials and returns a JWT.
- `200` → `{ "token": "<jwt>" }`
- `401` → invalid email or password

Both endpoints are rate-limited to **5 requests per minute per client IP**; exceeding the limit returns `429 Too Many Requests`.

### Authenticated requests

All endpoints other than `/api/health` and `/api/auth/**` require a valid JWT:
```
Authorization: Bearer <token>
```
Missing or invalid tokens return `401 Unauthorized`.

## Error responses

Errors share a consistent JSON shape across the API:
```json
{
  "timestamp": "2026-07-24T12:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/auth/login",
  "fieldErrors": null
}
```
