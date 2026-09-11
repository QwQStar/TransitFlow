package com.transitflow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic ingestTopic(AppProperties properties) {
        return TopicBuilder.name(properties.topics().ingest())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic dlqTopic(AppProperties properties) {
        return TopicBuilder.name(properties.topics().dlq())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
