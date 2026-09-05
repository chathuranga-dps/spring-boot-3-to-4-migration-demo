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
- springdoc-openapi 3.1.0
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
- `/swagger-ui/**`
- `/swagger-ui.html`
- `/v3/api-docs/**`

All `/api/**` endpoints require authentication.

## Swagger / OpenAPI

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Security scheme: HTTP Basic (`basicAuth`)

Use the **Authorize** button with `demo` / `demo123` to execute protected
Employee API operations.

```text
Spring Boot 3:
springdoc-openapi 2.8.14

Spring Boot 4:
springdoc-openapi 3.1.0
```

This is a good example of a third-party framework dependency that must be
upgraded together with Spring Boot even though the application's business APIs
do not change.

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
mvn test jacoco:report
```

### Test Results

```
Unit Tests:
  - EmployeeMapperTest
  - EmployeeServiceTest

Controller Tests:
  - EmployeeControllerTest (MockMvc, authentication scenarios)

Integration Tests:
  - EmployeeRepositoryIntegrationTest (Testcontainers with PostgreSQL)
```

## Migration Areas to Demonstrate

### 1. **Spring Boot Version Upgrade** (3.5.x → 4.x)

- **Current:** Spring Boot 3.5.x in `pom.xml` parent
- **Future:** Spring Boot 4.x
- **Validation:** Parent version change, then verify dependency compatibility

### 2. **Java Version Upgrade** (17 → 21)

- **Current:** Java 17 in `pom.xml` properties and `Dockerfile`
- **Future:** Java 21
- **Validation:** Update `<java.version>21</java.version>` and Docker image, recompile

### 3. **Spring Framework Version** (6.x → 7.x)

- **Current:** Spring Framework 6.x (managed by Boot 3 parent)
- **Future:** Spring Framework 7.x (managed by Boot 4 parent)
- **Validation:** Removed/deprecated API checks, see `config/SecurityConfig.java`

### 4. **Spring Security Version** (6.x → 7.x)

- **Location:** `config/SecurityConfig.java`
- **Current:** Spring Security 6.x API (SecurityFilterChain, authorizeHttpRequests)
- **Future:** Spring Security 7.x
- **Migration Note:** API is stable; validation ensures no deprecated methods

### 5. **Hibernate ORM Version** (6.x → 7.x)

- **Current:** Hibernate 6.x (managed by Spring Boot 3)
- **Validation Areas:** `repository/EmployeeRepository.java`
  - JPQL query: `findActiveEmployeesByDepartment()` with string interpolation
  - Native SQL query: `findHighEarners()` database compatibility
- **Future:** Hibernate 7.x compatibility

### 6. **Jakarta Persistence** (6.0+ → Latest)

- **Current:** `jakarta.persistence.*` in `entity/Employee.java`
- **Package:** Already using Jakarta (not legacy javax.persistence)
- **Validation:** Entity annotations, @GeneratedValue, @PrePersist, @PreUpdate

### 7. **Jackson Serialization** (2.x → 3.x) 🔴 MAJOR CHANGE

- **Location:** `config/JacksonConfig.java`
- **Current:** `com.fasterxml.jackson.databind.ObjectMapper` (Jackson 2)
- **Future:** Jackson 3.x with breaking API changes
- **Changes:**
  - Package names will change (still com.fasterxml but different organization)
  - Some deprecated APIs will be removed
  - Configuration methods may be renamed
- **Validation:** Update Jackson version, verify ObjectMapper creation and configuration

### Jackson Source-Code Migration Scenario

Spring Boot 3 currently contains custom Jackson 2 integration code using:

- `com.fasterxml.jackson.*`
- `@JsonComponent`
- `Jackson2ObjectMapperBuilderCustomizer`

These APIs are intentionally included so the Spring Boot 4 migration can demonstrate real Java source-code changes required for Jackson 3 compatibility.

### 8. **Database Drivers**

- **Default runtime:** H2 with a file-based database
- **Integration tests:** PostgreSQL JDBC driver and PostgreSQL Testcontainers module
- **Current:** Driver versions managed by the Spring Boot 3 parent
- **Future:** Driver versions managed by the Spring Boot 4 parent
- **Application Configuration:** `application.yml` datasource settings
- **Validation:** Verify H2 runtime behavior and PostgreSQL integration tests

### 9. **Web Framework** (Spring Web Starter)

- **Location:** `pom.xml` and `controller/EmployeeController.java`
- **Current:** Spring Boot 3 web starter + Tomcat 10.x
- **Future:** Spring Boot 4 may have different Servlet/Tomcat structure
- **Validation:** REST endpoints continue to work as expected

### 10. **Tomcat Servlet Container** (10.x → 11.x)

- **Current:** Tomcat 10.x (managed by Spring Boot 3)
- **Future:** Tomcat 11.x (managed by Spring Boot 4)
- **Impact:** Usually transparent; tested through integration tests

### 11. **Actuator Health Endpoints**

- **Location:** `health/DatabaseHealthIndicator.java`
- **Current:** Custom health indicator with Boot 3 Actuator
- **Validation:** Ensure `/actuator/health` continues to work

### 12. **Testing Framework** (JUnit 5 + Testcontainers)

- **Current:** JUnit 5 + Mockito + Testcontainers
- **Future:** Spring Boot 4 test stack
- **Validation:** Unit, controller, and integration tests must pass

---

## Spring Boot 3 → 4 Migration Roadmap

| Area                 | Current Demo            | Future Target    | Migration Consideration                       |
| -------------------- | ----------------------- | ---------------- | --------------------------------------------- |
| **Spring Boot**      | 3.5.x                   | 4.x              | Major platform upgrade                        |
| **Java**             | 17                      | 21               | JDK, CI/CD pipelines, Docker images           |
| **Spring Framework** | 6.x                     | 7.x              | Removed/deprecated APIs                       |
| **Spring Security**  | 6.x                     | 7.x              | Security authentication/authorization         |
| **Hibernate ORM**    | 6.x                     | 7.x              | JPQL queries, native queries, entity mappings |
| **Jackson**          | 2.x                     | 3.x              | JSON serialization, ObjectMapper API changes  |
| **Tomcat**           | 10.x                    | 11.x             | Servlet compatibility                         |
| **Database Drivers** | H2 + PostgreSQL tests   | Boot 4 managed   | Runtime and integration-test compatibility    |
| **Web Starter**      | spring-boot-starter-web | Boot 4 variant   | Controller, REST endpoint structure           |
| **Tests**            | Boot 3 + JUnit 5        | Boot 4 + JUnit 5 | Test compatibility                            |

---

## Management Demo Flow

This section describes the intended presentation sequence to demonstrate the migration.

### Phase 1: Demonstrate Current Baseline (Boot 3 Working)

1. **Build the Application**

   ```bash
   mvn clean package -DskipTests
   ```

   H2 is embedded, so no database service needs to be started.

2. **Start the Application**

   ```bash
   mvn spring-boot:run
   ```

3. **Create an Employee**

   ```bash
   curl -X POST http://localhost:8080/api/v1/employees \
     -H "Content-Type: application/json" \
     -u demo:demo123 \
     -d '{
       "employeeNumber": "EMP001",
       "firstName": "John",
       "lastName": "Smith",
       "email": "john.smith@example.com",
       "department": "Technology",
       "salary": 95000.00
     }'
   ```

4. **Retrieve the Employee**

   ```bash
   curl -u demo:demo123 http://localhost:8080/api/v1/employees/1
   ```

5. **Show Current Versions**

   ```bash
   java -version
   # Output: openjdk version "17.x.x" ...

   mvn -v
   # Output: Apache Maven 3.8.x ...
   ```

6. **Show Spring Boot and Dependency Versions**

   ```bash
   mvn dependency:tree | grep "spring-boot-starter-\|h2\|postgresql\|jackson\|hibernate"
   ```

   - Spring Boot: 3.5.0
   - H2: Boot 3 managed version
   - PostgreSQL JDBC: test scope
   - Jackson: 2.x
   - Hibernate: 6.x

7. **Check Actuator Health**
   ```bash
   curl http://localhost:8080/actuator/health | jq .
   ```

   - Status: UP
   - Database: UP
   - All components healthy ✅

---

### Phase 2: Migration Preparation (Change Dependencies)

9. **Stop Application**

   ```bash
   Ctrl+C
   ```

10. **Update Spring Boot Version (3.5.x → 4.x)**
    - Modify `pom.xml`: Change `spring-boot-starter-parent` version from `3.5.0` to `4.0.0` (or latest 4.x)

11. **Update Java Version (17 → 21)**
    - Modify `pom.xml`: Change `<java.version>17</java.version>` to `<java.version>21</java.version>`
    - Modify `Dockerfile`: Change `FROM eclipse-temurin:17-jre` to `FROM eclipse-temurin:21-jre`

12. **Attempt to Build**
    ```bash
    mvn clean compile
    ```

    - Expected: Compilation errors or warnings related to:
      - Jackson API changes (if Jackson 3.x is included)
      - Spring Security API changes (if minor breaking changes)
      - Hibernate changes (if new version has incompatibilities)

---

### Phase 3: Address Migration Issues

13. **Fix Compilation Errors**
    - Update `config/JacksonConfig.java` to use Jackson 3.x APIs
    - Update `config/SecurityConfig.java` if Spring Security 7 has API changes
    - Update `repository/EmployeeRepository.java` if Hibernate has query syntax issues

14. **Update Tests**
    - Verify `EmployeeRepositoryIntegrationTest` still works with Testcontainers
    - Update any test utilities if Spring Boot test utilities changed

15. **Build Again**
    ```bash
    mvn clean verify
    ```

    - All tests must pass
    - Zero compilation errors

---

### Phase 4: Validate Migrated Application

16. **Rebuild Application**

    ```bash
    mvn clean package
    ```

17. **Start Migrated Application**

    ```bash
    mvn spring-boot:run
    ```

18. **Verify Same API Endpoint Works**

    ```bash
    curl -u demo:demo123 http://localhost:8080/api/v1/employees/1
    ```

    - Returns: Same employee data as before ✅
    - Business logic unchanged ✅

19. **Verify Actuator Health**

    ```bash
    curl http://localhost:8080/actuator/health | jq .
    ```

    - Status: UP ✅

20. **Run All Tests One More Time**
    ```bash
    mvn test
    ```
mvn test
```

The test suite covers:

- Employee mapping
- Service behavior and duplicate detection
- Authenticated and unauthenticated MVC requests
- Public Swagger UI and OpenAPI JSON with documented Employee operations
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
| springdoc-openapi | 2.8.14 | 3.1.0 | Spring Boot 4 compatibility upgrade |
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
- Upgraded springdoc-openapi from 2.8.14 to 3.1.0 while preserving the
  documented API contract and HTTP Basic security scheme.
- Removed all legacy external-database dependencies and configuration.
- Migrated repository integration tests to H2.
- Preserved endpoint paths, authentication credentials, payloads, and response
  behavior.

See [SPRING_BOOT_4_MIGRATION_REPORT.md](SPRING_BOOT_4_MIGRATION_REPORT.md) for
the detailed migration record and validation results.
