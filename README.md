# AutoAndes API

Spring Boot 4 / Java 25 / Oracle. Backend repository for the AutoAndes exam.

## Run (PowerShell)

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot'
$env:DB_PASSWORD = '<local Oracle password>'
.\mvnw.cmd spring-boot:run
```

Defaults: `localhost:1522/FREEPDB1`, user `autoandesdb`, HTTP port `8080`.
Optional overrides: `DB_URL`, `DB_USERNAME`. Never commit local credentials.

## Current status: Questions 1 and 2

Services/vehicles CRUD and read-only customer lookup are implemented with DTOs,
validation, common errors, mutation/rejection logs and RN-01/RN-02 in the service layer.
Controllers never call repositories. Vehicle responses include `clienteId` and
`clienteNombre`, not a nested entity. The customer relationship is lazy.

Current business routes are public for Question 1/2 evaluation. JWT and permission
enforcement are added in Question 3; this is not the final security configuration.

Swagger UI: <http://localhost:8080/swagger-ui.html>
OpenAPI: <http://localhost:8080/v3/api-docs>

## Flyway and seed data

V1 preserves the supplied five-table creation SQL. Seed data is in V2 so that an
existing schema can be adopted without recreating its tables. The local tables were
inspected and empty before an explicit one-time baseline at version 1; V2 was then
applied. Fresh empty schemas run both migrations automatically.

V3 supplies the user/role tables omitted by the annex, using the fields from
section 3.1. Their JPA entities are included; account seeds and authentication
remain scheduled for Question 3. Fresh schemas apply V1, V2 and V3.
V4 adds the order-number sequence; a fresh schema applies all four migrations.

`baseline-on-migrate` stays false. Do not baseline an unknown schema or edit applied
migrations. Hibernate DDL changes are disabled. Future changes use new migrations.

Seed: 6 customers, 8 services (SRV-008 inactive), 8 vehicles, 3 REGISTRADA orders
and 1 ANULADA with their details. AAA-111 has mileage 45000. Seed orders are dated
the previous day to support the late-cancellation scenario in Question 2.
Identity values can have gaps after tests; use returned IDs instead of assuming IDs.

## API and evaluation

Base path: `/api/v1`. Lists are JSON arrays, not paginated.

| Method | Route | Success |
| --- | --- | --- |
| GET | `/clientes` | 200 |
| GET / POST | `/servicios` | 200 / 201 |
| GET / PUT / DELETE | `/servicios/{id}` | 200 / 200 / 204 |
| PATCH | `/servicios/{id}/estado` | 200 |
| GET / POST | `/vehiculos` | 200 / 201 |
| GET / PUT / DELETE | `/vehiculos/{id}` | 200 / 200 / 204 |

`GET /vehiculos?clienteId=...` filters by customer; no matches return `[]`.
Customers sort by name, services by code and vehicles by plate. POST includes Location.

Service create/update:

```json
{"codigo":"SRV-100","nombre":"Servicio de prueba","precio":95.50,"duracionMin":45}
```

New services are active. PATCH state body: `{"activo":false}`.
Vehicle create/update (use a real ID from GET /clientes):

```json
{"placa":"PST-100","marca":"Toyota","modelo":"Yaris","anio":2020,"clienteId":1}
```

400: invalid input; 404: missing resource/customer; 409: duplicate code/plate or
deletion blocked by orders. Referenced services must be deactivated via PATCH;
unused services can be physically deleted. All errors share timestamp/status/error/
mensaje/ruta. Only validation errors include `campos`. Unexpected errors return a
generic 500 and are logged on the server.

Required application users are seeded with BCrypt in Question 3: admin, asesor,
jefe, inactivo; test password `Auto2026*`, separate from Oracle credentials.

## Orders and reports (Question 2)

| Method | Route | Success |
| --- | --- | --- |
| POST | `/ordenes` | 201 with Location and complete order |
| GET | `/ordenes?placa&estado&desde&hasta` | 200 array |
| GET | `/ordenes/{id}` | 200 complete order |
| PATCH | `/ordenes/{id}/anular` | 200 canceled order |
| GET | `/reportes/ingresos-por-servicio?desde&hasta` | 200 aggregated array |

Order body (use actual vehicle/service IDs):

```json
{"vehiculoId":8,"kilometraje":50000,"detalles":[{"servicioId":1,"cantidad":2}]}
```

Unit price comes from Oracle. The service calculates subtotal and total using
BigDecimal; any client-supplied total is ignored. An order starts REGISTRADA.
Header and details are inserted inside one service transaction; a later inactive
or missing service rolls the entire transaction back. Detail count: 1–10; quantities:
1–5; services cannot repeat. Kilometraje is a nonnegative integer of up to 7 digits
and cannot be below the latest REGISTRADA order's value for the same vehicle.
A vehicle write lock serializes concurrent registrations for the mileage check.

V4 adds an Oracle sequence starting after the four seed order numbers. The format
is OS-000001. Numbers are unique and increasing; rollbacks can leave sequence gaps.
No number is reused and order IDs are independent of order numbers.

Cancellation body: `{"motivo":"Cancelación solicitada por el cliente"}`.
Only REGISTRADA orders from the same Lima calendar day can be canceled. Reasons
must contain 10–200 characters; padding spaces do not satisfy the minimum length.
Cancellation preserves detail rows and amounts for auditability.

Order filters are optional and combinable. Plate search is a case-insensitive
literal substring; estado is REGISTRADA or ANULADA. Dates use YYYY-MM-DD, including
the entire hasta day through an exclusive next-day boundary. Invalid ranges return
400. Results sort by fecha descending, then ID descending; empty results are `[]`.

The report uses a JPQL constructor projection and GROUP BY, entirely aggregated in
the database. Fields: codigo, servicio, cantidadTotal, importeTotal. Only REGISTRADA
orders count. Amounts sort descending, then service code. Date semantics match the
order filters, and omitted date limits are unbounded.

CORS allows only http://localhost:4200 for the API, methods GET/POST/PUT/PATCH/DELETE/
OPTIONS and Authorization/Content-Type/Accept headers. Location is exposed.
During Question 2, usuarioRegistro defaults to asesor for anonymous evaluation;
Question 3 replaces that fallback with the verified JWT principal.

## Verification

Run `.\mvnw.cmd test` with Java 25. Integration tests use isolated H2,
never the Oracle schema. They cover lifecycle operations, duplicate updates,
validation/error shapes, customer mapping/filtering and referenced deletion rules.

The Postman collection is also executed with Newman against Oracle. See
`docs/phase1-validation.md`, `docs/phase2-validation.md` and `postman/README.md`.
SQL Developer evidence queries are in `docs/phase2-oracle-verification.sql`.
