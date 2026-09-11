package com.transitflow.ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transitflow.config.AppProperties;
import com.transitflow.domain.MobilityEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AppProperties properties;

    public EventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            AppProperties properties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void publish(MobilityEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(properties.topics().ingest(), event.deviceId(), json);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize event", ex);
        }
    }
}
