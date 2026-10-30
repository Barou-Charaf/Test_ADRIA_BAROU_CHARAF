# Transaction Service — Step C2

This Spring Boot application implements the local Transaction Service scope from Step C2.

## Current C2 boundary

Transaction creation and transfers require live account data to verify account existence, status, currency, and balance.
OpenFeign communication is intentionally deferred to Step C3. Until that integration exists, valid create and transfer
requests return a clear integration-pending error and are not persisted. This prevents the service from reporting a
successful banking operation without performing the required account checks.

Read operations, persistence infrastructure, request validation, maximum-amount validation, REST contracts, GraphQL
contracts, and custom GraphQL scalar configuration are implemented in C2.

## Local endpoints

- REST: `http://localhost:8082/api/transactions`
- GraphQL: `http://localhost:8082/graphql`
- Swagger UI: `http://localhost:8082/swagger-ui.html`
- H2 console: `http://localhost:8082/h2-console`
- Health: `http://localhost:8082/actuator/health`

The local H2 JDBC URL is `jdbc:h2:mem:transactiondb`, with username `sa` and an empty password.
