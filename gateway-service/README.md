# Gateway Service

Spring Cloud Gateway for the ADRIA microservices application.

- Gateway port: `8080`
- Account REST: `/api/accounts/**`
- Transaction REST: `/api/transactions/**`
- Account GraphQL: `/account/graphql`
- Transaction GraphQL: `/transaction/graphql`

Routes are loaded from Config Server and resolved through Eureka.
