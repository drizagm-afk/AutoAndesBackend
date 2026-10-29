# Postman evaluation: Questions 1, 2 and 3

1. Start the backend on port 8080.
2. Import `AutoAndes.postman_collection.json` and `AutoAndes-Local.postman_environment.json`.
3. Select the **AutoAndes Local** environment.
4. Open the collection Runner and run the whole collection once, in its saved order:
   **01 - Auth**, **02 - Clientes**, **03 - Servicios**, **04 - Vehiculos**,
   **05 - Ordenes**, **06 - Reportes**, **07 - Errores y permisos**.
5. Expected: **84 requests, 205 assertions, zero failures**.

Login requests populate adminToken, asesorToken, jefeToken and the default token.
Start with Auth on every run, especially after restarting the API. CRUD uses admin,
order creation uses asesor and cancellation uses jefe. Security requests explicitly
override auth to send a restricted token, an altered token or no token.
The exported environment intentionally contains no saved tokens.

The first customer request generates a random service code and plate suffix. Tests save real
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
export the current collection and environment with all token values cleared.

Order checks include third-line inactive-service rollback, mileage 44999 vs the
45000 seed value, duplicate services, detail/quantity validation, missing references,
server-calculated totals, date/plate/state filters, cancellation restrictions, and
report amounts before and after cancellation. A supplied total of zero verifies
that stored amounts come from the backend. SQL rollback evidence queries are in
`../docs/phase2-oracle-verification.sql`.

Manual screenshots will be requested only when assembling final exam evidence.

Security checks cover all three active logins, login validation and identical 401
messages for bad credentials/inactive accounts, /auth/me, missing and altered tokens,
every forbidden advisor/manager mutation, the advisor's rejected report, and permitted
access after 403. Signed-expired-token rejection is also verified independently
against the running Oracle API and by the integration suite, without exporting a
signing key into Postman files. The collection never needs the JWT secret.
