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

## Phase 1 status

Services/vehicles CRUD and read-only customer lookup are implemented with DTOs,
validation, common errors, mutation/rejection logs and RN-01/RN-02 in the service layer.
Controllers never call repositories. Vehicle responses include `clienteId` and
`clienteNombre`, not a nested entity. The customer relationship is lazy.

Current CRUD routes are public for Question 1 evaluation. JWT and permission
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

## Verification

Run `.\mvnw.cmd test` with Java 25. Four HTTP integration tests use isolated H2,
never the Oracle schema. They cover lifecycle operations, duplicate updates,
validation/error shapes, customer mapping/filtering and referenced deletion rules.

The Postman collection was also executed with Newman against Oracle: 29 requests,
65 assertions, no failures. See `docs/phase1-validation.md` and `postman/README.md`.
