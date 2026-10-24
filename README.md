# Store Catalog Service

A small REST API for the catalog side of a delivery app: **stores** and the **menu
items** they offer. Built with Java 21, Spring Boot 3, Spring Data JPA, and PostgreSQL.

- Clean controller → service → repository layering with DTOs (no entities on the wire).
- List endpoints with pagination, sorting, and filtering (by store, price range,
  availability, category, name).
- Bean Validation on every input, with a `@RestControllerAdvice` that returns a
  consistent, structured error body.
- JUnit 5 + MockMvc for the web layer, and Testcontainers (real PostgreSQL) for the
  repository and end-to-end error/validation paths.
- GitHub Actions CI that runs the full Maven build and test suite.

## Requirements

- **JDK 21** (the project targets Java 21).
- **Docker** — only needed to run the integration tests (Testcontainers) and to run a
  local PostgreSQL. Not needed for the unit/web tests.
- Maven is provided via the bundled wrapper (`./mvnw`); no system install required.

## Quick start (no database needed)

To just run and explore the API locally, use the `local` profile, which uses an
in-memory H2 database — no Docker, no PostgreSQL to install:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Then open the interactive API docs at **http://localhost:8080/swagger-ui.html** to
browse every endpoint and try requests from the browser. (Data resets on each restart.)
You can also inspect the in-memory DB at http://localhost:8080/h2-console
(JDBC URL `jdbc:h2:mem:catalog`, user `sa`, blank password).

## Quick start (PostgreSQL)

### 1. Start PostgreSQL

```bash
docker run --name catalog-db -e POSTGRES_DB=catalog \
  -e POSTGRES_USER=catalog -e POSTGRES_PASSWORD=catalog \
  -p 5432:5432 -d postgres:16-alpine
```

### 2. Run the app

```bash
./mvnw spring-boot:run
```

The API is served at `http://localhost:8080`. Connection settings come from environment
variables (with the defaults matching the Docker command above):

| Variable       | Default                                   |
| -------------- | ----------------------------------------- |
| `DB_URL`       | `jdbc:postgresql://localhost:5432/catalog`|
| `DB_USERNAME`  | `catalog`                                 |
| `DB_PASSWORD`  | `catalog`                                 |
| `JPA_DDL_AUTO` | `update`                                  |

> **Schema management:** for convenience the app lets Hibernate create/update the schema
> (`JPA_DDL_AUTO=update`). A production deployment should instead manage the schema with a
> migration tool (Flyway/Liquibase) and set `JPA_DDL_AUTO=validate`.

## Running the tests

```bash
./mvnw test      # unit + web-layer tests (MockMvc). No Docker required.
./mvnw verify    # the above plus Testcontainers integration tests. Requires Docker.
```

Test naming convention: `*Test` = Surefire (fast, no Docker); `*IT` = Failsafe
(Testcontainers integration tests, run during `verify`).

## Data model

Two tables with a one-to-many relationship (a store has many menu items).

```
┌──────────────────────────┐          ┌──────────────────────────────┐
│ stores                   │          │ menu_items                   │
├──────────────────────────┤          ├──────────────────────────────┤
│ id          BIGINT  PK   │ 1      * │ id          BIGINT  PK        │
│ name        TEXT    NN   │─────────<│ store_id    BIGINT  FK → stores│
│ description TEXT         │          │ name        TEXT    NN        │
│ address     TEXT    NN   │          │ description TEXT              │
│ phone       TEXT        │          │ price       NUMERIC(10,2) NN   │
│ active      BOOLEAN NN   │          │ category    TEXT             │
│ created_at  TIMESTAMP NN │          │ available   BOOLEAN NN        │
│ updated_at  TIMESTAMP NN │          │ created_at  TIMESTAMP NN      │
│ version     BIGINT      │          │ updated_at  TIMESTAMP NN      │
└──────────────────────────┘          │ version     BIGINT           │
                                       └──────────────────────────────┘
```

`created_at` / `updated_at` are managed automatically via JPA auditing; `version` powers
optimistic locking.

## API

Base path: `/api/v1`. Request and response bodies are JSON. Interactive docs (Swagger
UI) are served at `/swagger-ui.html`, and the raw OpenAPI spec at `/v3/api-docs`.

### Stores

| Method   | Path                 | Description                      |
| -------- | -------------------- | -------------------------------- |
| `POST`   | `/stores`            | Create a store (→ `201`)         |
| `GET`    | `/stores/{id}`       | Get one store                    |
| `GET`    | `/stores`            | List stores (paged/filterable)   |
| `PUT`    | `/stores/{id}`       | Update a store                   |
| `DELETE` | `/stores/{id}`       | Delete a store (→ `204`)         |

List filters: `name` (contains, case-insensitive), `active` (boolean).

### Menu items

Nested under their owning store:

| Method   | Path                                      | Description               |
| -------- | ----------------------------------------- | ------------------------- |
| `POST`   | `/stores/{storeId}/menu-items`            | Create an item (→ `201`)  |
| `GET`    | `/stores/{storeId}/menu-items/{itemId}`   | Get one item              |
| `GET`    | `/stores/{storeId}/menu-items`            | List a store's items      |
| `PUT`    | `/stores/{storeId}/menu-items/{itemId}`   | Update an item            |
| `DELETE` | `/stores/{storeId}/menu-items/{itemId}`   | Delete an item (→ `204`)  |

Plus a cross-store search endpoint where the store is an optional filter:

| Method | Path          | Description                       |
| ------ | ------------- | --------------------------------- |
| `GET`  | `/menu-items` | Search items across all stores    |

Item list/search filters: `storeId`, `name`, `category`, `minPrice`, `maxPrice`,
`available`.

### Pagination & sorting

All list endpoints accept standard Spring pageable params:

- `page` (0-based, default `0`)
- `size` (default `20`, capped at `100`)
- `sort` (e.g. `sort=price,desc` or `sort=name,asc`)

Responses are wrapped:

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 3,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Error responses

Every handled error returns the same shape. Validation failures include `fieldErrors`:

```json
{
  "timestamp": "2026-10-23T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for request body",
  "path": "/api/v1/stores",
  "fieldErrors": [
    { "field": "name", "message": "must not be blank" }
  ]
}
```

| Status | When                                                        |
| ------ | ----------------------------------------------------------- |
| `400`  | Bean Validation failure, malformed JSON, bad param type     |
| `404`  | Store or menu item not found                                |
| `409`  | Data integrity conflict                                     |
| `500`  | Unexpected error                                            |

## Example requests

Create a store:

```bash
curl -i -X POST http://localhost:8080/api/v1/stores \
  -H 'Content-Type: application/json' \
  -d '{"name":"Pizza Palace","address":"1 Main St","phone":"555-0100"}'
```

Add a menu item (use the returned store id):

```bash
curl -i -X POST http://localhost:8080/api/v1/stores/1/menu-items \
  -H 'Content-Type: application/json' \
  -d '{"name":"Margherita","description":"Classic","price":9.50,"category":"Pizza"}'
```

List a store's available pizzas up to $10, cheapest first:

```bash
curl 'http://localhost:8080/api/v1/stores/1/menu-items?category=Pizza&available=true&maxPrice=10.00&sort=price,asc'
```

Search the whole catalog by price range (page 2, 10 per page):

```bash
curl 'http://localhost:8080/api/v1/menu-items?minPrice=5&maxPrice=20&page=1&size=10'
```

Trigger a validation error:

```bash
curl -i -X POST http://localhost:8080/api/v1/stores \
  -H 'Content-Type: application/json' \
  -d '{"name":"","address":""}'
# → 400 with fieldErrors for "name" and "address"
```

## Project layout

```
src/main/java/com/example/catalog/
├── CatalogApplication.java
├── common/
│   ├── JpaConfig.java           # enables JPA auditing
│   ├── error/                   # ApiError, GlobalExceptionHandler, exceptions
│   └── web/PageResponse.java    # paged-response wrapper
├── store/                       # Store entity, repo, service, controller, DTOs, specs
└── menu/                        # MenuItem entity, repo, service, controllers, DTOs, specs

src/test/java/com/example/catalog/
├── store/StoreControllerTest.java      # @WebMvcTest
├── menu/MenuItemControllerTest.java    # @WebMvcTest
└── integration/                        # Testcontainers (@DataJpaTest + @SpringBootTest)
```
