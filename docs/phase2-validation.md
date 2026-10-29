# Question 2 verification

Verified on 2026-10-29, America/Lima.

## Implementation

- One service-layer transaction persists an order header and every detail. Prices
  come from Oracle and all monetary calculations use BigDecimal in the service.
- RN-03/RN-04/RN-05: detail validation, no duplicate service, active services,
  last registered mileage, same-day cancellation and required reason.
- Oracle sequence for unique increasing OS-000001 numbers, with expected rollback gaps.
- Optional combined plate/state/date filters, date-descending order and empty arrays.
- Database-aggregated JPQL constructor projection with GROUP BY, excluding canceled orders.
- CORS for the Angular origin, all required methods and Authorization header.
- V4 migration applied successfully to Oracle; earlier migrations remain unchanged.

## Automated results

- Maven: 10 integration tests (4 CRUD + 6 order/report/CORS), 0 failures, 0 errors.
- Postman collection via Newman against Oracle: 57 requests, 124 assertions,
  0 failures. JSON output: `target/postman-phase2-results.json` (local build artifact).
- A supplied client total of zero was ignored; the created order returned the
  correct database-derived total of 280 and two persisted detail lines.
- Report amounts and quantities decreased by exactly the canceled order's values.
- Oracle CORS preflight: 200, allowed origin http://localhost:4200 and
  GET/POST/PUT/PATCH/DELETE/OPTIONS.

## Independent Oracle rollback evidence

After the Postman evaluation, a separate JDBC/HTTP check captured table counts,
submitted an order whose third detail was inactive SRV-008, and queried again:

```text
HTTP=409
Before: 6 order headers, 8 detail rows
After:  6 order headers, 8 detail rows
Header/detail total mismatches: 0
Orphan detail rows: 0
```

The first two detail rows and header are inserted before encountering the third
inactive service. Their rollback is guaranteed by @Transactional on registrar,
invoked through the Spring service proxy. The rejection is a RuntimeException.

The existing four seed orders were preserved. Two successful evaluation runs each
created and canceled one additional order, retained for audit. No successful order
is deleted because the contract provides no order-delete endpoint. Sequence/identity
gaps after rejected attempts are expected, not partial saved orders.

SQL Developer queries for final evidence are in `phase2-oracle-verification.sql`.
Manual Postman/screenshots are deferred to final exam evidence collection.

## Pending by design

JWT authentication, verified usuarioRegistro and permission enforcement belong to
Question 3. The current anonymous fallback asesor exists only for staged evaluation.
