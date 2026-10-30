package org.example.transactionservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionCreatedConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionCreatedConsumer.class);

    @KafkaListener(topics = TransactionCreatedKafkaPublisher.TOPIC)
    public void logTransactionCreated(TransactionCreatedEvent event) {
        LOGGER.info("Consumed TransactionCreated event: transactionId={}, accountId={}, amount={}, currency={}",
                event.id(), event.accountId(), event.amount(), event.currency());
    }
}
