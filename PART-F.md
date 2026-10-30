# Part F — Kafka

After a database transaction commits successfully, Transaction Service publishes a
`TransactionCreatedEvent` to `bank.transactions.created` using the account UUID as the message key.

A consumer in Transaction Service uses group `transaction-service-logger` and only logs the event.
It performs no banking write. A transfer persists two transaction records and therefore publishes
two events, one for each affected account.

## Configuration

The default broker is `localhost:9092`. Override it without changing source code when necessary:

```bash
export KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
```

The application declares the topic with one partition and one replica for the assignment's local
single-broker environment.
