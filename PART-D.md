# Part D — Technical Microservices

## Applications

| Service | Port | Purpose |
|---|---:|---|
| Config Service | 8888 | Centralized native configuration |
| Discovery Service | 8761 | Eureka registry |
| Gateway Service | 8080 | External REST and GraphQL routing |
| Account Service | 8081 | Account business operations |
| Transaction Service | 8082 | Transaction and transfer operations |

## Startup order

1. `config-service`
2. `discovery-service`
3. `account-servie`
4. `transaction-service`
5. `gateway-service`

Run each application from its own directory with `mvn spring-boot:run`.

## Gateway routes

- Account REST: `http://localhost:8080/api/accounts/**`
- Transaction REST: `http://localhost:8080/api/transactions/**`
- Account GraphQL: `http://localhost:8080/account/graphql`
- Transaction GraphQL: `http://localhost:8080/transaction/graphql`

The Gateway resolves `lb://` service names through Eureka. Transaction Service also resolves Account Service through
Eureka-backed OpenFeign rather than a fixed URL.
