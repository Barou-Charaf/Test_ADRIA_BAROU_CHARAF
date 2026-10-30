package org.example.transactionservice.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
public class KafkaTopicConfiguration {

    @Bean
    NewTopic transactionCreatedTopic() {
        return TopicBuilder.name(TransactionCreatedKafkaPublisher.TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
