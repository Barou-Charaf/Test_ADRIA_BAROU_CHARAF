# Transaction Service — Steps C2 and C3

This Spring Boot application implements the local Transaction Service scope from Step C2.

## Account-service integration

Transaction creation and transfers use OpenFeign and Eureka service discovery to verify account existence, ACTIVE status, currency, and balance.
Safe account reads use Resilience4J retry and circuit-breaker policies. Debit and credit operations use a separate
circuit breaker and are never automatically retried because a retry could duplicate a balance update.

The workflow is synchronous orchestration, not a distributed transaction. If persistence or a later transfer step fails,
the service attempts compensating account operations. A failed compensation is returned as an explicit error and is
never reported as success. This is a best-effort consistency strategy; production-grade guaranteed recovery would need
additional infrastructure that is outside the current assignment step.

A successful transfer stores two transaction rows—one associated with each account—because the assignment's fixed
Transaction entity has one `accountId` and no transaction-type or destination fields. No attributes were added to it.

## Local endpoints

- REST: `http://localhost:8082/api/transactions`
- GraphQL: `http://localhost:8082/graphql`
- Swagger UI: `http://localhost:8082/swagger-ui.html`
- H2 console: `http://localhost:8082/h2-console`
- Health: `http://localhost:8082/actuator/health`

The H2 and resilience settings are supplied by Config Server. The account-service location is resolved through Eureka.

## Kafka — Part F

After a database transaction commits successfully, each persisted transaction publishes a
`TransactionCreatedEvent` to `bank.transactions.created`. The Kafka record key is the account UUID.
A consumer in this service uses group `transaction-service-logger` and only logs received events;
it does not modify banking data.

Kafka defaults to `localhost:9092`. Override it when necessary:

```bash
export KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
```
