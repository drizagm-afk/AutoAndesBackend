# Postman evaluation: Question 1

1. Start the backend on port 8080.
2. Import `AutoAndes.postman_collection.json` and `AutoAndes-Local.postman_environment.json`.
3. Select the **AutoAndes Local** environment.
4. Open the collection Runner and run the whole collection once, in its saved order:
   **02 - Clientes**, **03 - Servicios**, **04 - Vehiculos**.
5. Expected: **29 requests, 65 assertions, zero failures**.

Auth, orders, reports and security folders are placeholders for later questions.
No login is required in Question 1. Token values are blank until Question 3.

The first request generates a random service code and plate suffix. Tests save real
IDs into the selected environment. Always run from the first request: later requests
may otherwise use deleted or unset IDs. Successful runs delete their own temporary
service and vehicle without modifying seed rows. If interrupted, inspect and remove
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
add login, 401, 403, orders and reports.
