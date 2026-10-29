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

## Phase 0 status

Project foundation only. No business endpoints or authentication implementation yet.
Flyway is deliberately disabled and Hibernate DDL is disabled while the manually
prepared schema and seed script are reconciled. Do not baseline an unknown schema.
The temporary Spring Security default is replaced in Question 3.

## API and evaluation

Business routes will use `/api/v1`. See `postman/` for the evolving collection and
environment. Required test accounts: admin, asesor, jefe, inactivo; their seed
password is `Auto2026*` (not the Oracle password). These accounts are not created
by Phase 0. Full endpoint documentation follows implementation.
