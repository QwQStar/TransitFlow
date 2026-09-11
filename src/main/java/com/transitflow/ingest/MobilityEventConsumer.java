package com.transitflow.ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transitflow.config.AppProperties;
import com.transitflow.domain.MobilityEvent;
import com.transitflow.store.IngestMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class MobilityEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MobilityEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final IngestService ingestService;
    private final IngestMetrics metrics;
    private final AppProperties properties;

    public MobilityEventConsumer(
            ObjectMapper objectMapper,
            IngestService ingestService,
            IngestMetrics metrics,
            AppProperties properties
    ) {
        this.objectMapper = objectMapper;
        this.ingestService = ingestService;
        this.metrics = metrics;
        this.properties = properties;
    }

    @KafkaListener(topics = "${transitflow.topics.ingest}", groupId = "transitflow-ingest")
    public void onIngest(String json, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        MobilityEvent event = read(json);
        ingestService.ingest(event);
        log.debug("ingested key={} event={}", key, event.uniqueKey());
    }

    @KafkaListener(topics = "${transitflow.topics.dlq}", groupId = "transitflow-dlq-counter")
    public void onDlq(String json) {
        metrics.markDlq();
        log.warn("DLQ record parked from topic {}: {}", properties.topics().dlq(), json);
    }

    private MobilityEvent read(String json) {
        try {
            MobilityEvent event = objectMapper.readValue(json, MobilityEvent.class);
            if (event.source() == null || event.deviceId() == null || event.eventId() == null) {
                throw new IllegalArgumentException("Missing required fields");
            }
            return event;
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid event JSON: " + json, ex);
        }
    }
}
