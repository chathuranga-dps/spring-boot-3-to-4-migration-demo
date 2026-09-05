# Spring Boot 3 to 4 Migration Demo

This branch contains the Spring Boot 4 version of an employee management REST
API. The migration modernizes the framework and runtime stack without changing
the REST API contract or employee business behavior.

The Spring Boot 3 baseline remains available on the `spring-3` branch. The
migrated application is on the `spring-4` branch.

## Technology Stack

- Java 21
- Spring Boot 4.1.1
- Spring Framework 7.0.9
- Spring Web MVC
- Spring Data JPA 4.1.1
- Hibernate ORM 7.4.5.Final
- Spring Security 7.1.1
- Jackson 3.1.5
- Tomcat 11.0.24
- H2 2.4.240
- Spring Boot Actuator
- Maven
- JUnit 5, Mockito, and MockMvc

## Architecture

```text
Controller Layer          -> REST endpoints, validation, and error handling
                |
Service Layer             -> Business logic and transaction boundaries
                |
Repository Layer          -> Spring Data JPA, JPQL, and native SQL
                |
Persistence Layer         -> File-based H2 database with Hibernate
                |
Configuration             -> Security, Jackson, Actuator, and DataSource
```

## Prerequisites

- JDK 21
- Maven 3.8.1 or newer

Verify the environment:

```bash
java -version
mvn -version
```

## Database

The application uses an embedded, persistent H2 database. No external database
or Docker service is required.

| Setting | Value |
| --- | --- |
| JDBC URL | `jdbc:h2:file:./data/employee_db` |
| Username | `sa` |
| Password | Empty |
| Data directory | `./data/` |
| Console URL | `http://localhost:8080/h2-console` |

Hibernate creates or updates the schema at startup. The application inserts
sample employees only when the table is empty.

For the H2 Console, use the JDBC URL and credentials shown above.

## Build and Run

Run the complete build:

```bash
mvn clean verify
```

Start the application:

```bash
mvn spring-boot:run
```

The service starts at `http://localhost:8080`.

## Authentication

Employee API endpoints use HTTP Basic authentication:

```text
Username: demo
Password: demo123
Role: USER
```

The following endpoints are public:

- `GET /actuator/health`
- `GET /actuator/info`
- `/h2-console/**`

All `/api/**` endpoints require authentication.

## API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/employees` | List employees |
| `GET` | `/api/v1/employees/{id}` | Get an employee |
| `POST` | `/api/v1/employees` | Create an employee |
| `PUT` | `/api/v1/employees/{id}` | Update an employee |
| `DELETE` | `/api/v1/employees/{id}` | Delete an employee |
| `GET` | `/api/v1/employees/search?department=Technology` | Search by department |
| `GET` | `/api/v1/employees/high-earners?salary=90000` | Find active high earners |

Example:

```bash
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -u demo:demo123 \
  -d '{
    "employeeNumber": "EMP010",
    "firstName": "Alice",
    "lastName": "Cooper",
    "email": "alice@example.com",
    "department": "Technology",
    "salary": 100000.00
  }'
```

## Actuator

The application exposes:

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`

Verify health:

```bash
curl http://localhost:8080/actuator/health
```

The overall status and custom database health component should report `UP`, and
the database product should report `H2`.

## Tests

Run all tests:

```bash
mvn test
```

The test suite covers:

- Employee mapping
- Service behavior and duplicate detection
- Authenticated and unauthenticated MVC requests
- Validation and exception responses
- Jackson date/time and null-property behavior
- Public Actuator endpoints
- H2 repository persistence, lookup, update, delete, JPQL, and native SQL

## Docker

Build the application and image:

```bash
mvn clean package
docker build -t spring-boot-migration-demo:1.0.0 .
```

The Docker image uses Eclipse Temurin Java 21. Mount the application working
directory or its `data` subdirectory when H2 data must persist outside the
container.

## Spring Boot 3 to 4 Migration Changes

The versions below were resolved from Maven on the `spring-3` and `spring-4`
branches.

| Area | Spring Boot 3 Branch | Spring Boot 4 Branch | Change |
| --- | --- | --- | --- |
| Java | 17 | 21 | Runtime and compiler baseline upgrade |
| Spring Boot | 3.5.0 | 4.1.1 | Major framework upgrade |
| Spring Framework | 6.2.7 | 7.0.9 | Framework generation upgrade |
| Spring Security | 6.5.0 | 7.1.1 | Security framework upgrade |
| Hibernate ORM | 6.6.15.Final | 7.4.5.Final | ORM upgrade |
| Jackson Databind | 2.19.0 | 3.1.5 | Jackson 2 to Jackson 3 migration |
| Tomcat | 10.1.41 | 11.0.24 | Servlet container upgrade |
| H2 | 2.3.232 | 2.4.240 | Managed database dependency upgrade |
| Spring Data JPA | 3.5.0 | 4.1.1 | Data access framework upgrade |
| Web starter | `spring-boot-starter-web` | `spring-boot-starter-webmvc` | Boot 4 modular starter |
| Test starters | `spring-boot-starter-test` | MVC and JPA test starters | Boot 4 modular test structure |

Key technical changes:

- Migrated the build and Docker runtime to Java 21.
- Replaced the general web starter with the Boot 4 MVC starter.
- Migrated Jackson configuration and tests to Jackson 3 `tools.jackson` APIs.
- Updated Actuator health imports for the Boot 4 health contributor module.
- Updated test annotation packages and replaced `@MockBean` with
  `@MockitoBean`.
- Added the Boot 4 H2 Console module and Spring Security frame support.
- Removed all legacy external-database dependencies and configuration.
- Migrated repository integration tests to H2.
- Preserved endpoint paths, authentication credentials, payloads, and response
  behavior.

See [SPRING_BOOT_4_MIGRATION_REPORT.md](SPRING_BOOT_4_MIGRATION_REPORT.md) for
the detailed migration record and validation results.
