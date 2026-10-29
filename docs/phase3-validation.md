# Question 3 verification

Verified on 2026-10-29, America/Lima.

- V5 applied successfully: ADMIN, ASESOR, JEFE_TALLER and four required accounts.
- All four stored BCrypt hashes independently checked against Auto2026*, with
  inactive account and exact role assignments confirmed in Oracle.
- JWT signature/issuer/expiration validation, one-hour validity and STATELESS API.
- Current active user and permission union loaded on every authenticated request.
- Order usuarioRegistro comes from the verified token; no anonymous fallback.
- Public exceptions limited to POST login, Swagger/OpenAPI and OPTIONS.
- Centralized permission matrix, hasAuthority and report @PreAuthorize.
- Common JSON error format for authentication (401) and authorization (403).

## Results

- 16 integration tests: 0 failures, 0 errors. Includes authenticated CRUD/order
  regressions, exact role restrictions, multi-role union, disabled-account rejection,
  invalid/altered/expired/missing-expiration tokens and unchanged access after 403.
- Newman against Oracle: 84 Postman requests, 205 assertions, 0 failures.
- Independent valid signed token: HTTP 200 from /auth/me.
- Independent expired signed token using the same key: HTTP 401, common error format.
- Wrong password, nonexistent username and inactive account return the same 401 message.
- Registration as asesor records usuarioRegistro=asesor; cancellation as jefe succeeds.
- Advisor cancellation/report and manager order/vehicle mutations return 403.

Full local run output is under target/postman-phase3-results.json and
target/postman-phase3-run.log (ignored build artifacts, containing runtime tokens).
Exported collection/environment have no saved real tokens or signing keys.
Manual screenshot capture remains deferred to final exam evidence collection.
