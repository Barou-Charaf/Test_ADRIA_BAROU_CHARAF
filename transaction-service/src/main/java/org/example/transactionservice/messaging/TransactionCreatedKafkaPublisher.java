package org.example.transactionservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TransactionCreatedKafkaPublisher {

    public static final String TOPIC = "bank.transactions.created";

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionCreatedKafkaPublisher.class);

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    public TransactionCreatedKafkaPublisher(KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(TransactionCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.accountId().toString(), event)
                .whenComplete((result, failure) -> {
                    if (failure == null) {
                        LOGGER.info("Published TransactionCreated event for transaction {}", event.id());
                    } else {
                        LOGGER.error("Failed to publish TransactionCreated event for transaction {}",
                                event.id(), failure);
                    }
                });
    }
}
