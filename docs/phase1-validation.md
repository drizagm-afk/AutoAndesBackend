# Question 1 verification

Verified against local Oracle on 2026-10-29 (America/Lima).

- The five supplied business tables existed, matched the SQL and were empty.
- Flyway baseline version 1 recorded explicitly; V2 seed migration applied successfully.
- V3 defines the omitted user/role tables; authentication and account seeds follow in Question 3.
- Maven: 4 HTTP integration tests, 0 failures, 0 errors (isolated H2 test schema).
- Postman collection executed with Newman against Oracle: 29 requests, 65 assertions,
  0 failures. Successful runs remove only their temporary service/vehicle records.
- Covered 200, 201, 204, 400, 404 and 409, field validation, duplicate creates/updates,
  customer filtering, flat customer DTO fields, state changes and referenced deletions.
- Authentication and permission checks are pending Question 3.

Full machine-readable Postman results are generated at `target/postman-phase1-results.json`
and are not committed because they are build artifacts. Screenshot instructions are
in `postman/README.md`.
