# Postman evaluation: Questions 1 and 2

1. Start the backend on port 8080.
2. Import `AutoAndes.postman_collection.json` and `AutoAndes-Local.postman_environment.json`.
3. Select the **AutoAndes Local** environment.
4. Open the collection Runner and run the whole collection once, in its saved order:
   **02 - Clientes**, **03 - Servicios**, **04 - Vehiculos**, **05 - Ordenes**, **06 - Reportes**.
5. The full collection has **57 requests**; every assertion should pass.

Auth and security folders are placeholders for Question 3.
No login is required yet. Token values are blank until Question 3.

The first request generates a random service code and plate suffix. Tests save real
IDs into the selected environment. Always run from the first request: later requests
may otherwise use deleted or unset IDs. Successful runs delete their own temporary
service and vehicle without modifying seed rows. Order evaluation intentionally
retains one new ANULADA order per successful run, using HHH-888, for auditability.
Do not expect order counts or sequence values to reset between runs.
If interrupted, inspect and remove
the test records before rerunning; do not delete seed records.

## Suggested screenshots

- Runner summary showing all checks passing.
- Create service: 201 with response and Location header.
- Edit service: 200; deactivate service: 200 with `activo: false`.
- Create/edit vehicle: 201/200 with `clienteNombre`.
- Invalid service: 400 with field messages in `campos`.
- Missing resource: 404 without `campos`.
- Duplicate plate: 409 with the backend message.
- Referenced service/vehicle deletion: 409.

Use request History or saved examples for successful create/edit responses after
Runner finishes: temporary records are deleted at the end. For final delivery,
export the current collection and environment with tokens cleared. Later phases
add login, 401 and 403 checks.

Order checks include third-line inactive-service rollback, mileage 44999 vs the
45000 seed value, duplicate services, detail/quantity validation, missing references,
server-calculated totals, date/plate/state filters, cancellation restrictions, and
report amounts before and after cancellation. A supplied total of zero verifies
that stored amounts come from the backend. SQL rollback evidence queries are in
`../docs/phase2-oracle-verification.sql`.

Manual screenshots will be requested only when assembling final exam evidence.
