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

## Current status: Questions 1, 2 and 3

Services/vehicles CRUD and read-only customer lookup are implemented with DTOs,
validation, common errors, mutation/rejection logs and RN-01/RN-02 in the service layer.
Controllers never call repositories. Vehicle responses include `clienteId` and
`clienteNombre`, not a nested entity. The customer relationship is lazy.

All business routes now require a signed, unexpired Bearer token and the required
permission. Only POST login, Swagger/OpenAPI and OPTIONS are public.

Swagger UI: <http://localhost:8080/swagger-ui.html>
OpenAPI: <http://localhost:8080/v3/api-docs>

## Flyway and seed data

V1 preserves the supplied five-table creation SQL. Seed data is in V2 so that an
existing schema can be adopted without recreating its tables. The local tables were
inspected and empty before an explicit one-time baseline at version 1; V2 was then
applied. Fresh empty schemas run both migrations automatically.

V3 supplies the user/role tables omitted by the annex, using the fields from
section 3.1. V4 adds the order-number sequence. V5 seeds roles and BCrypt-protected
accounts. A fresh empty schema applies all five migrations.

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

Required application users are seeded with BCrypt: admin, asesor,
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
usuarioRegistro comes exclusively from the verified JWT principal; the anonymous
evaluation fallback has been removed.

## Authentication and permissions (Question 3)

POST `/api/v1/auth/login` body:

```json
{"username":"asesor","password":"Auto2026*"}
```

Returns token, tipo (Bearer), expiraEnSegundos (3600), nombreCompleto, roles and
permisos. Send `Authorization: Bearer <token>` on protected requests.
GET `/api/v1/auth/me` returns id, username, nombreCompleto, roles and permisos.
Neither response exposes a password or hash. Invalid login fields return 400;
unknown user, wrong password and inactive account share the same 401 message.

| User | Password | Role | Active |
| --- | --- | --- | --- |
| admin | Auto2026* | ADMIN | Yes |
| asesor | Auto2026* | ASESOR | Yes |
| jefe | Auto2026* | JEFE_TALLER | Yes |
| inactivo | Auto2026* | ASESOR | No |

| Permission | ADMIN | ASESOR | JEFE_TALLER |
| --- | --- | --- | --- |
| SERVICIO_LEER | Yes | Yes | Yes |
| SERVICIO_ESCRIBIR | Yes | No | Yes |
| VEHICULO_LEER | Yes | Yes | Yes |
| VEHICULO_ESCRIBIR | Yes | Yes | No |
| ORDEN_LEER | Yes | Yes | Yes |
| ORDEN_REGISTRAR | Yes | Yes | No |
| ORDEN_ANULAR | Yes | No | Yes |
| REPORTE_VER | Yes | No | Yes |

Permissions are centralized in PermisosPorRol. Multiple roles receive their union.
SecurityConfig uses hasAuthority; the report also uses @PreAuthorize. Controllers
never compare role names. The filter validates signature, issuer and expiration,
then reloads the active account and current permissions from Oracle. JWT claims
include subject, issuer, issued-at, expiration, roles and permissions; claims are
signed, not encrypted. No HTTP session is created.

Missing, malformed, altered, expired or inactive-user tokens return common-format
401. An authenticated user missing a permission receives common-format 403; the
token remains valid for permitted operations.

Set optional `JWT_SECRET` to a Base64-encoded random key of at least 32 bytes to
retain token validity across restarts. No signing secret is committed. Without
this variable, a secure random development key is generated for each process;
restarting the API invalidates previous tokens. Re-login after restarting.
Swagger's Authorize button accepts the token without the Bearer prefix.

## Verification

Run `.\mvnw.cmd test` with Java 25. Integration tests use isolated H2,
never the Oracle schema. They cover lifecycle operations, duplicate updates,
validation/error shapes, customer mapping/filtering and referenced deletion rules.

The Postman collection is also executed with Newman against Oracle. See
`docs/phase1-validation.md`, `docs/phase2-validation.md`, `docs/phase3-validation.md`
and `postman/README.md`.
SQL Developer evidence queries are in `docs/phase2-oracle-verification.sql`.
